package spp.cli.commands.admin.permission

import com.apollographql.apollo.coroutines.await
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import permission.GetDeveloperPermissionsQuery
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import kotlin.system.exitProcess

class GetDeveloperPermissions : CliktCommand() {

    val id by argument(help = "Developer ID")

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetDeveloperPermissionsQuery(id)).await()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.developerPermissions.map { it.name })
        if (Main.standalone) exitProcess(0)
    }
}
