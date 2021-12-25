package spp.cli.commands.admin.role

import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.role.GetRolesQuery
import kotlin.system.exitProcess

class GetRoles : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.query(GetRolesQuery()).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.getRoles.map { it.roleName })
        if (Main.standalone) exitProcess(0)
    }
}
