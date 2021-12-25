package spp.cli.commands.admin.permission

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.permission.GetRolePermissionsQuery
import kotlin.system.exitProcess

class GetRolePermissions : CliktCommand() {

    val role by argument(help = "Role name")

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetRolePermissionsQuery(role)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.getRolePermissions.map { it.name })
        if (Main.standalone) exitProcess(0)
    }
}
