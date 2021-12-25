package spp.cli.commands.admin.access

import com.github.ajalt.clikt.core.CliktCommand
import io.vertx.core.json.Json
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.access.GetAccessPermissionsQuery
import kotlin.system.exitProcess

class GetAccessPermissions : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            PlatformCLI.apolloClient.query(GetAccessPermissionsQuery()).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(Json.encodePrettily(response.data!!.getAccessPermissions))
        if (Main.standalone) exitProcess(0)
    }
}
