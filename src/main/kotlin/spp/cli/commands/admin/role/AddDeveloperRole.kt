package spp.cli.commands.admin.role

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.role.AddDeveloperRoleMutation
import kotlin.system.exitProcess

class AddDeveloperRole : CliktCommand(printHelpOnEmptyArgs = true) {

    val id by argument(help = "Developer ID")
    val role by argument(help = "Role name")

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(AddDeveloperRoleMutation(id, role)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        if (response.data!!.addDeveloperRole) {
            if (Main.standalone) exitProcess(0)
        } else {
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
    }
}
