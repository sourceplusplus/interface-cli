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
package spp.cli

import com.github.ajalt.clikt.core.subcommands
import spp.cli.commands.Admin
import spp.cli.commands.Developer
import spp.cli.commands.Version
import spp.cli.commands.admin.Reset
import spp.cli.commands.admin.access.*
import spp.cli.commands.admin.client.AddClientAccess
import spp.cli.commands.admin.client.GetClientAccessors
import spp.cli.commands.admin.client.RefreshClientAccess
import spp.cli.commands.admin.client.RemoveClientAccess
import spp.cli.commands.admin.developer.AddDeveloper
import spp.cli.commands.admin.developer.GetDevelopers
import spp.cli.commands.admin.developer.RefreshAuthorizationCode
import spp.cli.commands.admin.developer.RemoveDeveloper
import spp.cli.commands.admin.permission.AddRolePermission
import spp.cli.commands.admin.permission.GetDeveloperPermissions
import spp.cli.commands.admin.permission.GetRolePermissions
import spp.cli.commands.admin.permission.RemoveRolePermission
import spp.cli.commands.admin.role.*
import spp.cli.commands.developer.*
import spp.cli.commands.developer.instrument.*
import spp.cli.commands.developer.view.AddView
import spp.cli.commands.developer.view.GetViews
import spp.cli.commands.developer.view.RemoveAllViews
import spp.cli.commands.developer.view.SubscribeView

object Main {

    private lateinit var args: Array<String>

    @JvmStatic
    fun main(args: Array<String>) {
        Main.args = args
        PlatformCLI.subcommands(
            //admin
            Admin().subcommands(
                //role
                AddRole(),
                GetDeveloperRoles(),
                GetRoles(),
                RemoveRole(),
                AddDeveloperRole(),
                RemoveDeveloperRole(),
                //permission
                AddRolePermission(),
                GetDeveloperPermissions(),
                GetRolePermissions(),
                RemoveRolePermission(),
                //developer
                AddDeveloper(),
                GetDevelopers(),
                RemoveDeveloper(),
                RefreshAuthorizationCode(),
                //client access
                AddClientAccess(),
                GetClientAccessors(),
                RemoveClientAccess(),
                RefreshClientAccess(),
                //instrument access
                AddAccessPermission(),
                AddRoleAccessPermission(),
                GetAccessPermissions(),
                GetDeveloperAccessPermissions(),
                GetRoleAccessPermissions(),
                RemoveAccessPermission(),
                RemoveRoleAccessPermission(),
                //etc
                Reset()
            ),
            Developer().subcommands(
                GetSelf()
            ),
            Add().subcommands(
                //instrument
                AddBreakpoint(),
                AddLog(),
                AddMeter(),
                AddSpan(),
                //view
                AddView()
            ),
            Get().subcommands(
                //instrument
                GetInstruments(),
                GetBreakpoints(),
                GetLogs(),
                GetMeters(),
                GetSpans(),
                //view
                GetViews()
            ),
            Remove().subcommands(
                //instrument
                RemoveInstrument(),
                RemoveInstruments(),
                RemoveAllInstruments(),
                //view
                RemoveAllViews()
            ),
            Subscribe().subcommands(
                //instrument
                SubscribeInstrument(),
                //view
                SubscribeView()
            ),
            //etc
            Version()
        ).main(args)
    }
}
