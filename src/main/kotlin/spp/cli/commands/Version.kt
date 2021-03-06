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
package spp.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import java.util.*

class Version : CliktCommand(help = "Display version information") {
    private val BUILD = ResourceBundle.getBundle("build")

    override fun run() {
        echo("spp-cli: " + BUILD.getString("build_version"))
        echo("Build id: " + BUILD.getString("build_id"))
        echo("Build date: " + BUILD.getString("build_date"))
    }
}
