package spp.cli.commands.instrument

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.instrument.RemoveLiveInstrumentsMutation
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class RemoveInstruments : CliktCommand() {

    val source by argument(help = "Qualified class name")
    val line by argument(help = "Line number").int()

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(RemoveLiveInstrumentsMutation(source, line)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(JsonCleaner.cleanJson(response.data!!.removeLiveInstruments).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
