/*
 * Source++, the open-source live coding platform.
 * Copyright (C) 2022 CodeBrig, Inc.
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
package spp.cli.commands.developer.instrument

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import eu.geekplace.javapinning.JavaPinning
import eu.geekplace.javapinning.pin.Pin
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.TrustOptions
import io.vertx.ext.bridge.BridgeEventType
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameParser
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI
import spp.protocol.SourceServices.Subscribe.toLiveInstrumentSubscriberAddress
import spp.protocol.extend.TCPServiceFrameParser
import spp.protocol.instrument.event.*

class SubscribeInstrument : CliktCommand(
    name = "instrument",
    help = "Listen to live instruments. Subscribes to all events by default"
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
        PlatformCLI.connectToPlatform()

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
                12800,
                PlatformCLI.platformHost.substringAfter("https://").substringAfter("http://")
                    .substringBefore(":")
            ).await()
            socket!!.handler(FrameParser(TCPServiceFrameParser(vertx, socket)))

            vertx.eventBus().consumer<JsonObject>(toLiveInstrumentSubscriberAddress(PlatformCLI.developer.id)) {
                val liveEvent = LiveInstrumentEvent(it.body())

                //todo: impl filter on platform
                if (instrumentIds.isNotEmpty()) {
                    when (liveEvent.eventType) {
                        LiveInstrumentEventType.LOG_HIT -> {
                            val logHit = LiveLogHit(JsonObject(liveEvent.data))
                            if (logHit.logId !in instrumentIds) {
                                return@consumer
                            }
                        }

                        LiveInstrumentEventType.BREAKPOINT_HIT -> {
                            val breakpointHit = LiveBreakpointHit(JsonObject(liveEvent.data))
                            if (breakpointHit.breakpointId !in instrumentIds) {
                                return@consumer
                            }
                        }

                        LiveInstrumentEventType.BREAKPOINT_REMOVED, LiveInstrumentEventType.LOG_REMOVED -> {
                            val logRemoved = LiveInstrumentRemoved(JsonObject(liveEvent.data))
                            if (logRemoved.liveInstrument.id !in instrumentIds) {
                                return@consumer
                            }
                        }

                        else -> TODO("Unhandled event type: ${liveEvent.eventType}")
                    }
                }

                if (!includeBreakpoints && !includeLogs && !includeMeters && !includeTraces) {
                    //listen for all events
                    println("\nType: ${liveEvent.eventType}\nData: ${liveEvent.data}")
                } else {
                    //todo: impl filtering on platform
                    //listen for specific events
                    if (includeBreakpoints && liveEvent.eventType.name.startsWith("breakpoint", true)) {
                        println("\nType: ${liveEvent.eventType}\nData: ${liveEvent.data}")
                    } else if (includeLogs && liveEvent.eventType.name.startsWith("log", true)) {
                        println("\nType: ${liveEvent.eventType}\nData: ${liveEvent.data}")
                    } else if (includeMeters && liveEvent.eventType.name.startsWith("meter", true)) {
                        println("\nType: ${liveEvent.eventType}\nData: ${liveEvent.data}")
                    } else if (includeTraces && liveEvent.eventType.name.startsWith("trace", true)) {
                        println("\nType: ${liveEvent.eventType}\nData: ${liveEvent.data}")
                    }
                }
            }

            //register listener
            FrameHelper.sendFrame(
                BridgeEventType.REGISTER.name.lowercase(),
                toLiveInstrumentSubscriberAddress(PlatformCLI.developer.id), null,
                JsonObject().apply { PlatformCLI.developer.accessToken?.let { put("auth-token", it) } },
                null, null, socket
            )
            println("Listening for events...")
        }
    }
}
