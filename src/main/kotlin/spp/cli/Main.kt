/*
 * Source++, the open-source live coding platform.
 * Copyright (C) 2022 CodeBrig, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package spp.cli

import com.github.ajalt.clikt.core.subcommands
import spp.cli.commands.Version
import spp.cli.commands.admin.Admin
import spp.cli.commands.admin.Reset
import spp.cli.commands.admin.access.*
import spp.cli.commands.admin.developer.AddDeveloper
import spp.cli.commands.admin.developer.GetDevelopers
import spp.cli.commands.admin.developer.RefreshDeveloperToken
import spp.cli.commands.admin.developer.RemoveDeveloper
import spp.cli.commands.admin.permission.AddRolePermission
import spp.cli.commands.admin.permission.GetDeveloperPermissions
import spp.cli.commands.admin.permission.GetRolePermissions
import spp.cli.commands.admin.permission.RemoveRolePermission
import spp.cli.commands.admin.role.*
import spp.cli.commands.developer.GetSelf
import spp.cli.commands.instrument.*
import spp.cli.commands.view.AddViewSubscription
import spp.cli.commands.view.ClearViewSubscriptions
import spp.cli.commands.view.GetViewSubscriptions
import spp.cli.commands.view.SubscribeView

object Main {

    var standalone = true
    lateinit var args: Array<String>

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
                RefreshDeveloperToken(),
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
            //instrument
            AddBreakpoint(),
            AddLog(),
            AddMeter(),
            AddSpan(),
            GetBreakpoints(),
            GetInstruments(),
            GetLogs(),
            GetMeters(),
            GetSpans(),
            RemoveInstrument(),
            RemoveInstruments(),
            ClearInstruments(),
            SubscribeEvents(),
            //view
            AddViewSubscription(),
            ClearViewSubscriptions(),
            GetViewSubscriptions(),
            SubscribeView(),
            //etc
            GetSelf(),
            Version()
        ).main(args)
    }
}
