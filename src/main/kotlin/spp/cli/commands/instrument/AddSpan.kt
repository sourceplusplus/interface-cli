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

class AddSpan : CliktCommand() {

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
