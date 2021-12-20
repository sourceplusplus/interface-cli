package spp.cli.commands.admin.permission

import com.apollographql.apollo.coroutines.await
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import permission.RemoveRolePermissionMutation
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import kotlin.system.exitProcess

class RemoveRolePermission : CliktCommand(printHelpOnEmptyArgs = true) {

    val role by argument(help = "Role name")
    val permission by argument(help = "Permission name")

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutate(RemoveRolePermissionMutation(role, permission)).await()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        if (response.data!!.removeRolePermission()) {
            if (Main.standalone) exitProcess(0)
        } else {
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
    }
}
