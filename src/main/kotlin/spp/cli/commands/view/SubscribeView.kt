package spp.cli.commands.view

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import eu.geekplace.javapinning.JavaPinning
import eu.geekplace.javapinning.pin.Pin
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.TrustOptions
import io.vertx.ext.bridge.BridgeEventType
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameParser
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI
import spp.protocol.SourceMarkerServices.Provide.LIVE_VIEW_SUBSCRIBER
import spp.protocol.artifact.log.Log
import spp.protocol.artifact.log.LogOrderType
import spp.protocol.artifact.log.LogResult
import spp.protocol.extend.TCPServiceFrameParser
import spp.protocol.view.LiveViewEvent

class SubscribeView : CliktCommand(
    help = "Listens for and outputs live views. Subscribes to all views by default"
) {

    val viewIds by argument(
        name = "View Subscription IDs",
        help = "Capture events from specific live views"
    ).multiple()
    val outputFullEvent by option(
        "--full",
        help = "Output full event data"
    ).flag()

    override fun run() {
        runBlocking {
            val vertx = Vertx.vertx()
            val client = if (PlatformCLI.certFingerprint != null) {
                val options = NetClientOptions()
                    .setReconnectAttempts(Int.MAX_VALUE).setReconnectInterval(5000)
                    .setSsl(PlatformCLI.platformHost.startsWith("https"))
                    .setTrustOptions(
                        TrustOptions.wrap(
                            JavaPinning.trustManagerForPins(listOf(Pin.fromString("CERTSHA256:${PlatformCLI.certFingerprint}")))
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
                5455,
                PlatformCLI.platformHost.substringAfter("https://").substringAfter("http://")
                    .substringBefore(":")
            ).await()
            socket!!.handler(FrameParser(TCPServiceFrameParser(vertx, socket)))

            vertx.eventBus().consumer<JsonObject>("local.$LIVE_VIEW_SUBSCRIBER.${PlatformCLI.developer.id}") {
                val event = Json.decodeValue(it.body().toString(), LiveViewEvent::class.java)
                if (outputFullEvent) {
                    println(JsonObject(event.metricsData))
                } else {
                    val rawMetrics = JsonObject(event.metricsData)
                    val logData = Json.decodeValue(rawMetrics.getJsonObject("log").toString(), Log::class.java)
                    val logsResult = LogResult(
                        event.artifactQualifiedName,
                        LogOrderType.NEWEST_LOGS,
                        logData.timestamp,
                        listOf(logData),
                        Int.MAX_VALUE
                    )
                    logsResult.logs.forEach {
                        println(it.getFormattedMessage())
                    }
                }
            }

            //register listener
            FrameHelper.sendFrame(
                BridgeEventType.REGISTER.name.lowercase(),
                "$LIVE_VIEW_SUBSCRIBER.${PlatformCLI.developer.id}",
                JsonObject(),
                socket
            )
            println("Listening for events...")
        }
    }
}
