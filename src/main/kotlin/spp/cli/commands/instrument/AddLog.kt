package spp.cli.commands.instrument

import com.apollographql.apollo3.api.Optional
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.instrument.AddLiveLogMutation
import spp.cli.protocol.type.InstrumentThrottleInput
import spp.cli.protocol.type.LiveLogInput
import spp.cli.protocol.type.LiveSourceLocationInput
import spp.cli.protocol.type.ThrottleStep
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class AddLog : CliktCommand() {

    val source by argument(help = "Qualified class name")
    val line by argument(help = "Line number").int()
    val logFormat by argument(help = "Log format")
    val logArguments by option("-logArgument", "-l", help = "Log argument").multiple()
    val condition by option("-condition", "-c", help = "Trigger condition")
    val expiresAt by option("-expiresAt", "-e", help = "Expiration time (epoch time [ms])").long()
    val hitLimit by option("-hitLimit", "-h", help = "Trigger hit limit").int()
    val throttleLimit by option("-throttleLimit", "-t", help = "Trigger throttle limit").int().default(1)
    val throttleStep by option("-throttleStep", "-s", help = "Trigger throttle step").enum<ThrottleStep>()
        .default(ThrottleStep.SECOND)

    override fun run() = runBlocking {
        val input = LiveLogInput(
            logFormat = logFormat,
            logArguments = Optional.Present(logArguments),
            location = LiveSourceLocationInput(source, line),
            condition = Optional.Present(condition),
            expiresAt = Optional.Present(expiresAt),
            hitLimit = Optional.Present(hitLimit),
            throttle = Optional.Present(InstrumentThrottleInput(throttleLimit, throttleStep))
        )
        val response = try {
            apolloClient.mutation(AddLiveLogMutation(input)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(JsonCleaner.cleanJson(response.data!!.addLiveLog).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
