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
package spp.cli.commands.admin.role

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI
import spp.cli.PlatformCLI.apolloClient
import spp.cli.protocol.role.RemoveDeveloperRoleMutation
import spp.cli.util.ExitManager.exitProcess

class RemoveDeveloperRole : CliktCommand(printHelpOnEmptyArgs = true) {

    val id by argument(help = "Developer ID")
    val role by argument(help = "Role name")

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(RemoveDeveloperRoleMutation(id, role)).execute()
        } catch (e: Exception) {
            exitProcess(-1, e)
        }
        if (response.hasErrors()) {
            exitProcess(response.errors!!)
        }

        if (PlatformCLI.verbose) {
            if (response.data!!.removeDeveloperRole) {
                echo("Removed role $role from developer $id")
            } else {
                echo("Failed to remove role $role from developer $id, do they have it?", err = true)
            }
        } else {
            echo(response.data!!.removeDeveloperRole)
        }
        if (response.data!!.removeDeveloperRole) {
            exitProcess(0)
        } else {
            exitProcess(-1, response.errors)
        }
    }
}
