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
package spp.cli.util

import com.apollographql.apollo3.api.Error
import com.github.ajalt.clikt.output.TermUi
import spp.cli.PlatformCLI.currentContext
import spp.cli.PlatformCLI.echoError

object ExitManager {

    var standalone = true

    fun exitProcess(status: Int, e: Exception): Nothing {
        echoError(e)
        if (standalone) kotlin.system.exitProcess(status)
        throw e
    }

    fun exitProcess(status: Int, e: List<Error>? = null) {
        if (e != null) {
            echo(e[0].message, err = true)
        }
        if (standalone) kotlin.system.exitProcess(status)
    }

    fun exitProcess(e: List<Error>): Nothing {
        echo(e[0].message, err = true)
        if (standalone) exitProcess(-1)
        throw Exception(e[0].message)
    }

    private fun echo(
        message: Any?,
        trailingNewline: Boolean = true,
        err: Boolean = false,
        lineSeparator: String = currentContext.console.lineSeparator,
    ) {
        TermUi.echo(message, trailingNewline, err, currentContext.console, lineSeparator)
    }
}
