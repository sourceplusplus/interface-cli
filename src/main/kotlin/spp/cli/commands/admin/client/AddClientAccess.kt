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
package spp.cli.commands.admin.client

import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI
import spp.cli.PlatformCLI.apolloClient
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.client.AddClientAccessMutation
import kotlin.system.exitProcess

class AddClientAccess : CliktCommand() {

    override fun run() = runBlocking {
        val response = try {
            apolloClient.mutation(AddClientAccessMutation()).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        if (PlatformCLI.verbose) {
            echo("Added client access");
            echo("Id: ${response.data!!.addClientAccess.id!!}")
            echo("Secret: ${response.data!!.addClientAccess.secret!!}")
        } else {
            echo(response.data!!.addClientAccess.id!!)
            echo(response.data!!.addClientAccess.secret!!)
        }
        if (Main.standalone) exitProcess(0)
    }
}
