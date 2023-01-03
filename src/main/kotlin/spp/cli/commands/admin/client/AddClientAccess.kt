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
package spp.cli.commands.admin.client

import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI
import spp.cli.PlatformCLI.apolloClient
import spp.cli.protocol.client.AddClientAccessMutation
import spp.cli.util.ExitManager.exitProcess

class AddClientAccess : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(AddClientAccessMutation()).execute()
        } catch (e: Exception) {
            exitProcess(-1, e)
        }
        if (response.hasErrors()) {
            exitProcess(response.errors!!)
        }

        if (PlatformCLI.verbose) {
            echo("Added client access")
            echo("Id: ${response.data!!.addClientAccess.id!!}")
            echo("Secret: ${response.data!!.addClientAccess.secret!!}")
        } else {
            echo(response.data!!.addClientAccess.id!!)
            echo(response.data!!.addClientAccess.secret!!)
        }
        exitProcess(0)
    }
}
