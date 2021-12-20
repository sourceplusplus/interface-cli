package spp.cli.commands.admin.role

import com.apollographql.apollo.coroutines.await
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import role.GetRolesQuery
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import kotlin.system.exitProcess

class GetRoles : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetRolesQuery()).await()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.roles.map { it.roleName() })
        if (Main.standalone) exitProcess(0)
    }
}
