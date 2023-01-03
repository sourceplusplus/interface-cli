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
package spp.cli.commands.developer.view

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import eu.geekplace.javapinning.JavaPinning
import eu.geekplace.javapinning.pin.Pin
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.TrustOptions
import io.vertx.ext.bridge.BridgeEventType
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameParser
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI
import spp.protocol.artifact.log.Log
import spp.protocol.artifact.log.LogOrderType
import spp.protocol.artifact.log.LogResult
import spp.protocol.service.SourceServices.Subscribe.toLiveViewSubscriberAddress
import spp.protocol.service.extend.TCPServiceFrameParser
import spp.protocol.view.LiveViewEvent
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder

class SubscribeView : CliktCommand(
    name = "view",
    help = "Listen to live views. Subscribes to all views by default"
) {

    companion object {
        private val formatter = DateTimeFormatterBuilder()
            .appendPattern("yyyyMMddHHmm")
            .toFormatter()
            .withZone(ZoneOffset.UTC)
    }

//    val viewIds by argument(
//        name = "View Subscription IDs",
//        help = "Capture events from specific live views"
//    ).multiple()
    val outputFullEvent by option(
        "--full",
        help = "Output full event data"
    ).flag()

    override fun run() {
        PlatformCLI.connectToPlatform()

        runBlocking {
            val vertx = Vertx.vertx()
            val client = if (PlatformCLI.certFingerprint != null) {
                val options = NetClientOptions()
                    .setReconnectAttempts(Int.MAX_VALUE).setReconnectInterval(5000)
                    .setSsl(PlatformCLI.platformHost.startsWith("https"))
                    .setTrustOptions(
                        TrustOptions.wrap(
                            JavaPinning.trustManagerForPins(
                                listOf(Pin.fromString("CERTSHA256:${PlatformCLI.certFingerprint}"))
                            )
                        )
                    )
                vertx.createNetClient(options)
            } else {
                val options = NetClientOptions()
                    .setTrustAll(true)
                    .setReconnectAttempts(Int.MAX_VALUE).setReconnectInterval(5000)
                    .setSsl(PlatformCLI.platformHost.startsWith("https"))
                vertx.createNetClient(options)
            }
            val socket = client.connect(
                12800,
                PlatformCLI.platformHost.substringAfter("https://").substringAfter("http://")
                    .substringBefore(":")
            ).await()
            socket!!.handler(FrameParser(TCPServiceFrameParser(vertx, socket)))

            vertx.eventBus().consumer<JsonObject>(toLiveViewSubscriberAddress(PlatformCLI.developer.id)) {
                val event = LiveViewEvent(it.body())
                if (outputFullEvent) {
                    println(event.metricsData)
                } else {
                    when (event.viewConfig.viewName) {
                        "LOG" -> outputLogEvent(event)
                        "ACTIVITY" -> outputActivityEvent(event)
                        else -> println(JsonObject(event.metricsData).encodePrettily())
                    }
                }
            }

            //register listener
            FrameHelper.sendFrame(
                BridgeEventType.REGISTER.name.lowercase(),
                toLiveViewSubscriberAddress(PlatformCLI.developer.id), null,
                JsonObject().apply { PlatformCLI.developer.accessToken?.let { put("auth-token", it) } },
                null, null, socket
            )
            println("Listening for events...")
        }
    }

    private fun outputActivityEvent(event: LiveViewEvent) {
        val metrics = JsonArray(event.metricsData)
        for (i in 0 until metrics.size()) {
            val metric = metrics.getJsonObject(i)
            val eventTime = LocalDateTime.from(formatter.parse(metric.getString("timeBucket")))
            var value: String? = null
            if (metric.getNumber("percentage") != null) {
                value = (metric.getNumber("percentage").toDouble() / 100.0).toString() + "%"
            }
            if (value == null) value = metric.getNumber("value").toString()

            val metricType = metric.getJsonObject("meta").getString("metricsName")
            println("$eventTime ${metric.getString("entityName")} ($metricType): $value")
        }
    }

    private fun outputLogEvent(event: LiveViewEvent) {
        val rawMetrics = JsonObject(event.metricsData)
        val logData = Log(rawMetrics.getJsonObject("log"))
        val logsResult = LogResult(
            event.artifactQualifiedName,
            LogOrderType.NEWEST_LOGS,
            logData.timestamp,
            listOf(logData),
            Int.MAX_VALUE
        )
        logsResult.logs.forEach {
            println(it.toFormattedMessage())
        }
    }
}
