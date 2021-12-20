package spp.cli.commands.developer

import com.apollographql.apollo.api.ScalarTypeAdapters
import com.apollographql.apollo.api.internal.SimpleResponseWriter
import com.apollographql.apollo.coroutines.await
import com.github.ajalt.clikt.core.CliktCommand
import developer.GetSelfQuery
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class GetSelf : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetSelfQuery()).await()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.self.let {
            val respWriter = SimpleResponseWriter(ScalarTypeAdapters.DEFAULT)
            it.marshaller().marshal(respWriter)
            JsonCleaner.cleanJson(JsonObject(respWriter.toJson("")).getJsonObject("data")).encodePrettily()
        })
        if (Main.standalone) exitProcess(0)
    }
}
