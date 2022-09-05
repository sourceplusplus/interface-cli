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
package spp.cli.commands.developer.view

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.type.LiveViewConfigInput
import spp.cli.protocol.type.LiveViewSubscriptionInput
import spp.cli.protocol.view.AddLiveViewSubscriptionMutation
import spp.cli.protocol.view.adapter.AddLiveViewSubscriptionMutation_ResponseAdapter.AddLiveViewSubscription
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class AddView : CliktCommand(name = "view", help = "Add a live view subscription") {

    val entityIds by argument(name = "Entity IDs").multiple(required = true)
    val viewName by argument(name = "View Name")
    val viewMetrics by argument(name = "View Metrics").multiple(required = true)
    val refreshRateLimit by option("-r", "--refresh-rate-limit", help = "Refresh rate limit (in seconds)").int()

    override fun run() = runBlocking {
        val input = LiveViewSubscriptionInput(
            entityIds = entityIds.toList(),
            liveViewConfig = LiveViewConfigInput(
                viewName = viewName,
                viewMetrics = viewMetrics,
                refreshRateLimit ?: -1
            )
        )
        val response = try {
            apolloClient.mutation(AddLiveViewSubscriptionMutation(input)).execute()
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
            AddLiveViewSubscription.toJson(it, CustomScalarAdapters.Empty, response.data!!.addLiveViewSubscription)
            it.endObject()
            (it.root() as LinkedHashMap<*, *>)
        }).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
