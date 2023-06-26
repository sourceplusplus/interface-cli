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
package integration

import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import spp.cli.Main
import java.util.*

class CommandParseTest : CLIIntegrationTest() {

    @Test
    fun `run all commands`() {
        Main.main(
            "-v version".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-developers".split(" ").toTypedArray()
        )
        val newDev = UUID.randomUUID().toString().replace("-", "")
        Main.main(
            "-v admin add-developer $newDev".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin refresh-authorization-code $newDev".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-roles".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-role-permissions role_manager".split(" ").toTypedArray()
        )
        val newRole = UUID.randomUUID().toString().replace("-", "")
        Main.main(
            "-v admin add-role $newRole".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin add-role-permission $newRole ADD_DEVELOPER".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin add-developer-role $newDev $newRole".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-developer-roles $newDev".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-developer-permissions $newDev".split(" ").toTypedArray()
        )

        val origOut = System.out
        val interceptor = Interceptor(origOut)
        System.setOut(interceptor)
        Main.main(
            "-v admin add-access-permission -l spp.example.webapp.model.User WHITE_LIST".split(" ").toTypedArray()
        )
        val accessPermissionId = JsonObject(interceptor.output.toString()).getString("id")
        interceptor.clear()

        Main.main(
            "-v admin add-role-access-permission $newRole $accessPermissionId".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-access-permissions".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-developer-access-permissions $newDev".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-role-access-permissions $newRole".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-role-access-permission $newRole $accessPermissionId".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-access-permission $accessPermissionId".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-developer-role $newDev $newRole".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-role-permission $newRole ADD_DEVELOPER".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-role $newRole".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-developer $newDev".split(" ").toTypedArray()
        )
        Main.main(
            "-v add breakpoint -h 100 -l 48 spp.example.webapp.model.User".split(" ").toTypedArray()
        )
        Main.main(
            "-v add log -h 100 -l 48 spp.example.webapp.model.User test-message".split(" ").toTypedArray()
        )
        Main.main(
            "-v get instruments".split(" ").toTypedArray()
        )
        Main.main(
            "-v get breakpoints".split(" ").toTypedArray()
        )
        Main.main(
            "-v get logs".split(" ").toTypedArray()
        )
        Main.main(
            "-v remove instruments spp.example.webapp.model.User 48".split(" ").toTypedArray()
        )
        System.setOut(origOut)
    }
}
