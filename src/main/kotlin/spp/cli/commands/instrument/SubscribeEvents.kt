package spp.cli.commands.instrument

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
import spp.protocol.SourceMarkerServices
import spp.protocol.extend.TCPServiceFrameParser
import spp.protocol.instrument.LiveInstrumentEvent

class SubscribeEvents : CliktCommand(
    help = "Listens for and outputs live events. Subscribes to all events by default"
) {

    val instrumentIds by argument(
        name = "Instrument IDs",
        help = "Capture events from specific live instruments"
    ).multiple()
    val includeBreakpoints by option("--breakpoints", "-b", help = "Include live breakpoint events")
        .flag(default = false)
    val includeLogs by option("--logs", "-l", help = "Include live log events")
        .flag(default = false)
    val includeMetrics by option("--metrics", "-m", help = "Include live metric events")
        .flag(default = false)
    val includeTraces by option("--traces", "-t", help = "Include live trace events")
        .flag(default = false)

    override fun run() {
        if (!includeBreakpoints && !includeLogs && !includeMetrics && !includeTraces) {
            //listen for all events
        } else {
            //listen for specific events
        }

        var eventCount = 1
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

            vertx.eventBus().consumer<JsonObject>("local." + SourceMarkerServices.Provide.LIVE_INSTRUMENT_SUBSCRIBER) {
                val event = Json.decodeValue(it.body().toString(), LiveInstrumentEvent::class.java)
                println(
                    "\nEvent (${eventCount++}):\n" +
                            "\tType: ${event.eventType}\n" +
                            "\tData: ${event.data}"
                )
            }

            //register listener
            FrameHelper.sendFrame(
                BridgeEventType.REGISTER.name.toLowerCase(),
                SourceMarkerServices.Provide.LIVE_INSTRUMENT_SUBSCRIBER,
                JsonObject(),
                socket
            )
            println("Listening for events...")
        }
    }
}
