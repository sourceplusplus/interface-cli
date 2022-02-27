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
import spp.cli.commands.Admin
import spp.cli.commands.Developer
import spp.cli.commands.Version
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
import spp.cli.commands.developer.*
import spp.cli.commands.developer.instrument.*
import spp.cli.commands.developer.view.AddView
import spp.cli.commands.developer.view.GetViews
import spp.cli.commands.developer.view.RemoveAllViews
import spp.cli.commands.developer.view.SubscribeView

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
