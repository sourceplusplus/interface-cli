package spp.cli.commands.view

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.type.LiveViewSubscriptionInput
import spp.cli.protocol.view.AddLiveViewSubscriptionMutation
import spp.cli.protocol.view.adapter.AddLiveViewSubscriptionMutation_ResponseAdapter.AddLiveViewSubscription
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class AddViewSubscription : CliktCommand() {

    val entityIds by argument(name = "Entity IDs").multiple(required = true)

    override fun run() = runBlocking {
        val input = LiveViewSubscriptionInput(
            entityIds = entityIds.toList(),
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
