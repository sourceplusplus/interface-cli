/*
 * Source++, the continuous feedback platform for developers.
 * Copyright (C) 2022-2023 CodeBrig, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spp.cli

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.network.okHttpClient
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import eu.geekplace.javapinning.JavaPinning
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.util.encoders.Hex
import java.io.File
import java.io.StringReader
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object PlatformCLI : CliktCommand(name = "spp-cli", allowMultipleSubcommands = true) {

    val verbose by option("-v", "--verbose", help = "Enable verbose mode").flag()
    val platformHost: String by option("-p", "--platform", help = "Source++ platform host")
        .default(
            (if (System.getenv("SPP_HTTP_SSL_ENABLED") == "true") "https://" else "http://")
                    + (System.getenv("SPP_PLATFORM_HOST") ?: "localhost") + ":12800"
        )
    private val platformCertificate by option("-c", "--certificate", help = "Source++ platform certificate").file()
        .default(File("config/spp-platform.crt"))
    private val platformKey by option("-k", "--key", help = "Source++ platform key").file()
        .default(File("config/spp-platform.key"))
    private val authorizationCode by option("-a", "--authorization-code", help = "Developer authorization code")
    val apolloClient: ApolloClient by lazy { connectToPlatform() }
    val certFingerprint: String? by lazy {
        if (platformCertificate.exists()) {
            val crtFileData = platformCertificate.readText()
            val crtParser = PEMParser(StringReader(crtFileData))
            val crtHolder = crtParser.readObject() as X509CertificateHolder
            val certificate = JcaX509CertificateConverter().getCertificate(crtHolder)!!
            fingerprint(certificate)
        } else {
            null
        }
    }
    var developerId: String = "system"
    var accessToken: String? = null

    override fun run() = Unit

    fun connectToPlatform(): ApolloClient {
        val serverUrl = if (platformHost.startsWith("http")) {
            platformHost
        } else {
            "https://$platformHost"
        }

        val httpClient = if (certFingerprint != null) {
            OkHttpClient().newBuilder()
                .hostnameVerifier { _, _ -> true }
                .sslSocketFactory(
                    JavaPinning.forPin("CERTSHA256:$certFingerprint").socketFactory,
                    JavaPinning.trustManagerForPins("CERTSHA256:$certFingerprint")
                )
                .build()
        } else {
            val naiveTrustManager = object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
            }
            val insecureSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
                val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
                init(null, trustAllCerts, SecureRandom())
            }.socketFactory

            OkHttpClient().newBuilder()
                .hostnameVerifier { _, _ -> true }
                .sslSocketFactory(insecureSocketFactory, naiveTrustManager)
                .build()
        }

        val jwtToken: String?
        if (platformKey.exists()) {
            val keyPair = PEMParser(StringReader(platformKey.readText())).use {
                JcaPEMKeyConverter().getKeyPair(it.readObject() as PEMKeyPair)
            }
            val algorithm = Algorithm.RSA256(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey)
            jwtToken = JWT.create()
                .withIssuer("cli")
                .withClaim("developer_id", "system") //users with key are automatically considered system
                .withClaim("created_at", Instant.now().toEpochMilli())
                .withClaim("expires_at", Instant.now().plus(8760, ChronoUnit.HOURS).toEpochMilli())
                .sign(algorithm)
            developerId = "system"
        } else {
            val tokenUri = "$serverUrl/api/new-token?authorization_code=$authorizationCode"
            val resp = httpClient.newCall(Request.Builder().url(tokenUri).build()).execute()
            if (resp.code in 200..299) {
                jwtToken = resp.body!!.string()

                if (resp.code != 202) {
                    val decoded = JWT.decode(jwtToken)
                    developerId = decoded.getClaim("developer_id").asString()
                    accessToken = jwtToken
                }
            } else if (resp.code == 401 && authorizationCode.isNullOrEmpty()) {
                error("Connection failed. Reason: Missing access token")
            } else if (resp.code == 401) {
                error("Connection failed. Reason: Invalid access token")
            } else {
                error("Connection failed. Reason: " + resp.message)
            }
        }

        return ApolloClient.Builder()
            .okHttpClient(
                httpClient.newBuilder().addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("Authorization", "Bearer $jwtToken")
                            .build()
                    )
                }.build()
            )
            .serverUrl("$serverUrl/graphql/spp")
            .build()
    }

    fun echoError(e: Exception) {
        when (e) {
            is ApolloException -> {
                echo(e.cause!!.message, err = true)
                if (verbose) {
                    echo(e.cause!!.stackTraceToString(), err = true)
                }
            }

            else -> {
                echo(e.message, err = true)
                if (verbose) {
                    echo(e.stackTraceToString(), err = true)
                }
            }
        }
    }

    private fun fingerprint(c: X509Certificate): String {
        val der = c.encoded
        val sha1 = sha256DigestOf(der)
        val hexBytes: ByteArray = Hex.encode(sha1)
        val hex = String(hexBytes).toUpperCase()
        val fp = StringBuffer()
        var i = 0
        fp.append(hex.substring(i, i + 2))
        while (2.let { i += it; i } < hex.length) {
            fp.append(':')
            fp.append(hex.substring(i, i + 2))
        }
        return fp.toString()
    }

    private fun sha256DigestOf(input: ByteArray): ByteArray {
        val d = SHA256Digest()
        d.update(input, 0, input.size)
        val result = ByteArray(d.digestSize)
        d.doFinal(result, 0)
        return result
    }
}
