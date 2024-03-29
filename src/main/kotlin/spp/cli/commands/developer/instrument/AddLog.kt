/*
 * Source++, the continuous feedback platform for developers.
 * Copyright (C) 2022-2023 CodeBrig, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spp.cli.commands.developer.instrument

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI.apolloClient
import spp.cli.protocol.instrument.AddLiveLogMutation
import spp.cli.protocol.instrument.adapter.AddLiveLogMutation_ResponseAdapter.AddLiveLog
import spp.cli.protocol.type.InstrumentThrottleInput
import spp.cli.protocol.type.LiveLogInput
import spp.cli.protocol.type.LiveSourceLocationInput
import spp.cli.util.ExitManager.exitProcess
import spp.cli.util.JsonCleaner
import spp.protocol.instrument.throttle.ThrottleStep

class AddLog : CliktCommand(name = "log", help = "Add a live log instrument") {

    val source by argument(help = "Qualified class name")
    val logFormat by argument(help = "Log format")
    val line by option("-line", "-l", help = "Line number").int()
    val logArguments by option("-arguments", "-a", help = "Log arguments").multiple()
    val id by option("-id", "-i", help = "Log identifier")
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
            location = LiveSourceLocationInput(source, Optional.presentIfNotNull(line)),
            condition = Optional.Present(condition),
            expiresAt = Optional.Present(expiresAt),
            hitLimit = Optional.Present(hitLimit),
            throttle = Optional.Present(
                InstrumentThrottleInput(
                    throttleLimit, spp.cli.protocol.type.ThrottleStep.valueOf(throttleStep.toString())
                )
            ),
            id = Optional.presentIfNotNull(id)
        )
        val response = try {
            apolloClient.mutation(AddLiveLogMutation(input)).execute()
        } catch (e: Exception) {
            exitProcess(-1, e)
        }
        if (response.hasErrors()) {
            exitProcess(response.errors!!)
        }

        echo(JsonCleaner.cleanJson(MapJsonWriter().let {
            it.beginObject()
            AddLiveLog.toJson(it, CustomScalarAdapters.Empty, response.data!!.addLiveLog)
            it.endObject()
            (it.root() as LinkedHashMap<*, *>)
        }).encodePrettily())
        exitProcess(0)
    }
}
