/*
 * Source++, the continuous feedback platform for developers.
 * Copyright (C) 2022 CodeBrig, Inc.
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
package spp.cli.commands.admin.access

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.json.MapJsonWriter
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
import spp.cli.protocol.access.adapter.AddAccessPermissionMutation_ResponseAdapter.AddAccessPermission
import spp.cli.util.JsonCleaner
import spp.protocol.platform.auth.AccessType
import kotlin.system.exitProcess

class AddAccessPermission : CliktCommand(printHelpOnEmptyArgs = true) {

    val locationPatterns by option("-locationPattern", "-l", help = "Location pattern").multiple(required = true)
    val type by argument(help = "Access permission type").enum<AccessType>()

    override fun run() = runBlocking {
        val response = try {
            PlatformCLI.apolloClient.mutation(
                AddAccessPermissionMutation(
                    Optional.Present(locationPatterns),
                    spp.cli.protocol.type.AccessType.valueOf(type.toString())
                )
            ).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        if (PlatformCLI.verbose) {
            echo(JsonCleaner.cleanJson(MapJsonWriter().let {
                it.beginObject()
                AddAccessPermission.toJson(it, CustomScalarAdapters.Empty, response.data!!.addAccessPermission)
                it.endObject()
                (it.root() as LinkedHashMap<*, *>)
            }).encodePrettily())
        } else {
            echo("Added access permission with id ${response.data!!.addAccessPermission.id}")
            echo("Location patterns: ${response.data!!.addAccessPermission.locationPatterns!!}")
            echo("Type: ${response.data!!.addAccessPermission.type}")
        }
        if (Main.standalone) exitProcess(0)
    }
}
