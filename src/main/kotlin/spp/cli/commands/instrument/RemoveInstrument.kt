package spp.cli.commands.instrument

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.instrument.RemoveLiveInstrumentMutation
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class RemoveInstrument : CliktCommand(printHelpOnEmptyArgs = true) {

    val id by argument(help = "Instrument ID")

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(RemoveLiveInstrumentMutation(id)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(JsonCleaner.cleanJson(response.data!!.removeLiveInstrument).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
