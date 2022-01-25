package spp.cli.commands.view

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.view.GetLiveViewSubscriptionsQuery
import spp.cli.protocol.view.adapter.GetLiveViewSubscriptionsQuery_ResponseAdapter.GetLiveViewSubscription
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class GetViewSubscriptions : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetLiveViewSubscriptionsQuery()).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(JsonCleaner.cleanJson(MapJsonWriter().let {
            it.beginArray()
            response.data!!.getLiveViewSubscriptions.forEach { ob ->
                it.beginObject()
                GetLiveViewSubscription.toJson(it, CustomScalarAdapters.Empty, ob)
                it.endObject()
            }
            it.endArray()
            (it.root() as ArrayList<*>)
        }).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
