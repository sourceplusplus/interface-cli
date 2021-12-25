package spp.cli.commands.admin

import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.system.ResetMutation
import kotlin.system.exitProcess

class Reset : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(ResetMutation()).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.reset)
        if (Main.standalone) exitProcess(0)
    }
}
