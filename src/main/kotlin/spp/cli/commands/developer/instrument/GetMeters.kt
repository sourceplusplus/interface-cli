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
package spp.cli.commands.developer.instrument

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI.apolloClient
import spp.cli.protocol.instrument.GetLiveMetersQuery
import spp.cli.protocol.instrument.adapter.GetLiveMetersQuery_ResponseAdapter.GetLiveMeter
import spp.cli.util.ExitManager.exitProcess
import spp.cli.util.JsonCleaner

class GetMeters : CliktCommand(name = "meters", help = "Get live meter instruments") {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetLiveMetersQuery()).execute()
        } catch (e: Exception) {
            exitProcess(-1, e)
        }
        if (response.hasErrors()) {
            exitProcess(response.errors!!)
        }

        echo(JsonCleaner.cleanJson(MapJsonWriter().let {
            it.beginArray()
            response.data!!.getLiveMeters.forEach { ob ->
                it.beginObject()
                GetLiveMeter.toJson(it, CustomScalarAdapters.Empty, ob)
                it.endObject()
            }
            it.endArray()
            (it.root() as ArrayList<*>)
        }).encodePrettily())
        exitProcess(0)
    }
}
