package spp.cli.commands.instrument

import com.apollographql.apollo.coroutines.await
import com.github.ajalt.clikt.core.CliktCommand
import instrument.ClearLiveInstrumentsMutation
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import kotlin.system.exitProcess

class ClearInstruments : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutate(ClearLiveInstrumentsMutation()).await()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!)
        if (Main.standalone) exitProcess(0)
    }
}
