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

class CommandParseTest : CLIIntegrationTest() {

    @Test
    fun `run all commands`() {
        Main.main(
            "-v version".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-developers".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin add-developer test".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin refresh-authorization-code test".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-roles".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-role-permissions role_manager".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin add-role tester".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin add-role-permission tester ADD_DEVELOPER".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin add-developer-role test tester".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-developer-roles test".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-developer-permissions test".split(" ").toTypedArray()
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
            "-v admin add-role-access-permission tester $accessPermissionId".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-access-permissions".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-developer-access-permissions test".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin get-role-access-permissions tester".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-role-access-permission tester $accessPermissionId".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-access-permission $accessPermissionId".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-developer-role test tester".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-role-permission tester ADD_DEVELOPER".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-role tester".split(" ").toTypedArray()
        )
        Main.main(
            "-v admin remove-developer test".split(" ").toTypedArray()
        )
        Main.main(
            "-v add breakpoint -h 100 spp.example.webapp.model.User 48".split(" ").toTypedArray()
        )
        Main.main(
            "-v add log -h 100 spp.example.webapp.model.User 48 test-message".split(" ").toTypedArray()
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
        Main.main(
            "-v admin reset".split(" ").toTypedArray()
        )
    }
}
