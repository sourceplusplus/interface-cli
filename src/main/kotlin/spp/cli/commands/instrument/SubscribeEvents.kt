/*
 * Source++, the open-source live coding platform.
 * Copyright (C) 2022 CodeBrig, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import spp.protocol.ProtocolMarshaller.deserializeLiveInstrumentRemoved
import spp.protocol.SourceServices
import spp.protocol.extend.TCPServiceFrameParser
import spp.protocol.instrument.event.LiveBreakpointHit
import spp.protocol.instrument.event.LiveInstrumentEvent
import spp.protocol.instrument.event.LiveInstrumentEventType
import spp.protocol.instrument.event.LiveLogHit

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
    val includeMeters by option("--meters", "-m", help = "Include live meter events")
        .flag(default = false)
    val includeTraces by option("--traces", "-t", help = "Include live trace events")
        .flag(default = false)

    override fun run() {
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

            vertx.eventBus().consumer<JsonObject>("local." + SourceServices.Provide.LIVE_INSTRUMENT_SUBSCRIBER) {
                val liveEvent = Json.decodeValue(it.body().toString(), LiveInstrumentEvent::class.java)

                //todo: impl filter on platform
                if (instrumentIds.isNotEmpty()) {
                    when (liveEvent.eventType) {
                        LiveInstrumentEventType.LOG_HIT -> {
                            val logHit = Json.decodeValue(liveEvent.data, LiveLogHit::class.java)
                            if (logHit.logId !in instrumentIds) {
                                return@consumer
                            }
                        }
                        LiveInstrumentEventType.BREAKPOINT_HIT -> {
                            val breakpointHit = Json.decodeValue(liveEvent.data, LiveBreakpointHit::class.java)
                            if (breakpointHit.breakpointId !in instrumentIds) {
                                return@consumer
                            }
                        }
                        LiveInstrumentEventType.BREAKPOINT_REMOVED, LiveInstrumentEventType.LOG_REMOVED -> {
                            val logRemoved = deserializeLiveInstrumentRemoved(JsonObject(liveEvent.data))
                            if (logRemoved.liveInstrument.id !in instrumentIds) {
                                return@consumer
                            }
                        }
                        else -> TODO("Unhandled event type: ${liveEvent.eventType}")
                    }
                }

                if (!includeBreakpoints && !includeLogs && !includeMeters && !includeTraces) {
                    //listen for all events
                    println(
                        "\nEvent (${eventCount++}):\n" +
                                "\tType: ${liveEvent.eventType}\n" +
                                "\tData: ${liveEvent.data}"
                    )
                } else {
                    //todo: impl filtering on platform
                    //listen for specific events
                    if (includeBreakpoints && liveEvent.eventType.name.startsWith("breakpoint", true)) {
                        println(
                            "\nEvent (${eventCount++}):\n" +
                                    "\tType: ${liveEvent.eventType}\n" +
                                    "\tData: ${liveEvent.data}"
                        )
                    } else if (includeLogs && liveEvent.eventType.name.startsWith("log", true)) {
                        println(
                            "\nEvent (${eventCount++}):\n" +
                                    "\tType: ${liveEvent.eventType}\n" +
                                    "\tData: ${liveEvent.data}"
                        )
                    } else if (includeMeters && liveEvent.eventType.name.startsWith("meter", true)) {
                        println(
                            "\nEvent (${eventCount++}):\n" +
                                    "\tType: ${liveEvent.eventType}\n" +
                                    "\tData: ${liveEvent.data}"
                        )
                    } else if (includeTraces && liveEvent.eventType.name.startsWith("trace", true)) {
                        println(
                            "\nEvent (${eventCount++}):\n" +
                                    "\tType: ${liveEvent.eventType}\n" +
                                    "\tData: ${liveEvent.data}"
                        )
                    }
                }
            }

            //register listener
            FrameHelper.sendFrame(
                BridgeEventType.REGISTER.name.lowercase(),
                SourceServices.Provide.LIVE_INSTRUMENT_SUBSCRIBER, null,
                JsonObject().apply { PlatformCLI.developer.accessToken?.let { put("auth-token", it) } },
                null, null, socket
            )
            println("Listening for events...")
        }
    }
}
