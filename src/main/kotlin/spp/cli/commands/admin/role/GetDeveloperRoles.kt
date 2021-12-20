package spp.cli.commands.admin.role

import com.apollographql.apollo.coroutines.await
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import role.GetDeveloperRolesQuery
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import kotlin.system.exitProcess

class GetDeveloperRoles : CliktCommand() {

    val id by argument(help = "Developer ID")

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetDeveloperRolesQuery(id)).await()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.developerRoles.map { it.roleName() })
        if (Main.standalone) exitProcess(0)
    }
}
