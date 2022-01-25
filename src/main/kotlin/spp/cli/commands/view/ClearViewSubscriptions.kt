package spp.cli.commands.view

import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.view.ClearLiveViewSubscriptionsMutation
import kotlin.system.exitProcess

class ClearViewSubscriptions : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(ClearLiveViewSubscriptionsMutation()).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.clearLiveViewSubscriptions)
        if (Main.standalone) exitProcess(0)
    }
}
