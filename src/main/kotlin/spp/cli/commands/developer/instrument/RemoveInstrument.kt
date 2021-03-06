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
package spp.cli.commands.developer.instrument

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.instrument.RemoveLiveInstrumentMutation
import spp.cli.protocol.instrument.adapter.RemoveLiveInstrumentMutation_ResponseAdapter.RemoveLiveInstrument
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class RemoveInstrument : CliktCommand(name = "instrument", printHelpOnEmptyArgs = true) {

    val id by argument(help = "Instrument ID")

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(RemoveLiveInstrumentMutation(id)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        if (response.data!!.removeLiveInstrument != null) {
            echo(JsonCleaner.cleanJson(MapJsonWriter().let {
                it.beginObject()
                RemoveLiveInstrument.toJson(it, CustomScalarAdapters.Empty, response.data!!.removeLiveInstrument!!)
                it.endObject()
                (it.root() as LinkedHashMap<*, *>)
            }).encodePrettily())
        } else {
            echo(JsonObject().encodePrettily())
        }
        if (Main.standalone) exitProcess(0)
    }
}
