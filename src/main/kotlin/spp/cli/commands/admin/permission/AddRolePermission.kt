package spp.cli.commands.admin.permission

import com.apollographql.apollo.coroutines.await
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.enum
import kotlinx.coroutines.runBlocking
import permission.AddRolePermissionMutation
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import type.RolePermission
import kotlin.system.exitProcess

class AddRolePermission : CliktCommand(printHelpOnEmptyArgs = true) {

    val role by argument(help = "Role name")
    val permission by argument(help = "Permission name").enum<RolePermission>()

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutate(AddRolePermissionMutation(role, permission.name)).await()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        if (response.data!!.addRolePermission()) {
            if (Main.standalone) exitProcess(0)
        } else {
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
    }
}
