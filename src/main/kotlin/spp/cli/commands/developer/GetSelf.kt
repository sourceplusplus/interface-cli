package spp.cli.commands.developer

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.developer.GetSelfQuery
import spp.cli.protocol.developer.adapter.GetSelfQuery_ResponseAdapter.GetSelf
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class GetSelf : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetSelfQuery()).execute()
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
            GetSelf.toJson(it, CustomScalarAdapters.Empty, response.data!!.getSelf)
            it.endObject()
            (it.root() as LinkedHashMap<*, *>)
        }).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
