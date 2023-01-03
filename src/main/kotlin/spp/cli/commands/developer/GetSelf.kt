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
package spp.cli.commands.developer

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI.apolloClient
import spp.cli.protocol.developer.GetSelfQuery
import spp.cli.protocol.developer.adapter.GetSelfQuery_ResponseAdapter.GetSelf
import spp.cli.util.ExitManager.exitProcess
import spp.cli.util.JsonCleaner

class GetSelf : CliktCommand(help = "Get the current developer's profile") {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetSelfQuery()).execute()
        } catch (e: Exception) {
            exitProcess(-1, e)
        }
        if (response.hasErrors()) {
            exitProcess(response.errors!!)
        }

        echo(JsonCleaner.cleanJson(MapJsonWriter().let {
            it.beginObject()
            GetSelf.toJson(it, CustomScalarAdapters.Empty, response.data!!.getSelf)
            it.endObject()
            (it.root() as LinkedHashMap<*, *>)
        }).encodePrettily())
        exitProcess(0)
    }
}
