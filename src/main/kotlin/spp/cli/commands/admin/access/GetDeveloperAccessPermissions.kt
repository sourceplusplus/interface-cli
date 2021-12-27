package spp.cli.commands.admin.access

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.access.GetDeveloperAccessPermissionsQuery
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class GetDeveloperAccessPermissions : CliktCommand(printHelpOnEmptyArgs = true) {

    val id by argument(help = "Developer ID")

    override fun run() = runBlocking {
        val response = try {
            PlatformCLI.apolloClient.query(GetDeveloperAccessPermissionsQuery(id)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(JsonCleaner.cleanJson(response.data!!.getDeveloperAccessPermissions).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
