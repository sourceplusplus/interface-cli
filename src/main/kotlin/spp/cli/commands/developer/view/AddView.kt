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

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI.apolloClient
import spp.cli.protocol.type.LiveViewConfigInput
import spp.cli.protocol.type.LiveViewInput
import spp.cli.protocol.view.AddLiveViewMutation
import spp.cli.protocol.view.adapter.AddLiveViewMutation_ResponseAdapter.AddLiveView
import spp.cli.util.ExitManager.exitProcess
import spp.cli.util.JsonCleaner
import java.util.*

class AddView : CliktCommand(name = "view", help = "Add a live view") {

    val entityIds by argument(name = "Entity IDs").multiple(required = true)
    val viewName by option("-name", "-n", help = "View name").default("cli-" + UUID.randomUUID().toString())
    val viewMetrics by option("-metrics", "-m", help = "View metrics").multiple()

    override fun run() = runBlocking {
        val input = LiveViewInput(
            entityIds = entityIds.toList(),
            viewConfig = LiveViewConfigInput(
                viewName,
                viewMetrics.ifEmpty { entityIds }
            )
        )
        val response = try {
            apolloClient.mutation(AddLiveViewMutation(input)).execute()
        } catch (e: Exception) {
            exitProcess(-1, e)
        }
        if (response.hasErrors()) {
            exitProcess(response.errors!!)
        }

        echo(JsonCleaner.cleanJson(MapJsonWriter().let {
            it.beginObject()
            AddLiveView.toJson(it, CustomScalarAdapters.Empty, response.data!!.addLiveView)
            it.endObject()
            (it.root() as LinkedHashMap<*, *>)
        }).encodePrettily())
        exitProcess(0)
    }
}
