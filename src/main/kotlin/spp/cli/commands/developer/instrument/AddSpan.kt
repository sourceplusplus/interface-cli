/*
 * Source++, the continuous feedback platform for developers.
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

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.instrument.AddLiveSpanMutation
import spp.cli.protocol.instrument.adapter.AddLiveSpanMutation_ResponseAdapter.AddLiveSpan
import spp.cli.protocol.type.LiveSpanInput
import spp.cli.protocol.type.LiveSpanLocationInput
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class AddSpan : CliktCommand(name = "span", help = "Add a live span instrument") {

    val source by argument(help = "Qualified function name")
    val operationName by argument(help = "Operation name")
//    val condition by option("-condition", "-c", help = "Trigger condition")
//    val expiresAt by option("-expiresAt", "-e", help = "Expiration time (epoch time [ms])").long()
//    val hitLimit by option("-hitLimit", "-h", help = "Trigger hit limit").int()
//    val throttleLimit by option("-throttleLimit", "-t", help = "Trigger throttle limit").int().default(1)
//    val throttleStep by option("-throttleStep", "-s", help = "Trigger throttle step").enum<ThrottleStep>()
//        .default(ThrottleStep.SECOND)

    override fun run() = runBlocking {
        val input = LiveSpanInput(
            operationName = operationName,
            location = LiveSpanLocationInput(source),
//            condition = Optional.Present(condition),
//            expiresAt = Optional.Present(expiresAt),
//            hitLimit = Optional.Present(hitLimit),
//            throttle = Optional.Present(InstrumentThrottleInput(throttleLimit, throttleStep))
        )
        val response = try {
            apolloClient.mutation(AddLiveSpanMutation(input)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(JsonCleaner.cleanJson(MapJsonWriter().let {
            it.beginObject()
            AddLiveSpan.toJson(it, CustomScalarAdapters.Empty, response.data!!.addLiveSpan)
            it.endObject()
            (it.root() as LinkedHashMap<*, *>)
        }).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
