package spp.cli.commands.admin.access

import com.apollographql.apollo3.api.Optional
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.access.AddAccessPermissionMutation
import spp.cli.protocol.type.AccessType
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class AddAccessPermission : CliktCommand(printHelpOnEmptyArgs = true) {

    val locationPatterns by option("-locationPattern", "-l", help = "Location pattern").multiple(required = true)
    val type by argument(help = "Access permission type").enum<AccessType>()

    override fun run() = runBlocking {
        val response = try {
            PlatformCLI.apolloClient.mutation(
                AddAccessPermissionMutation(Optional.Present(locationPatterns), type)
            ).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(JsonCleaner.cleanJson(response.data!!.addAccessPermission).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
