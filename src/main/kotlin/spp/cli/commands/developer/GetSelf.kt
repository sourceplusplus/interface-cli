package spp.cli.commands.developer

import com.github.ajalt.clikt.core.CliktCommand
import io.vertx.core.json.Json
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.developer.GetSelfQuery
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

        echo(Json.encodePrettily(response.data!!.getSelf))
        if (Main.standalone) exitProcess(0)
    }
}
