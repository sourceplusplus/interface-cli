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
package integration

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.jackson.DatabindCodec
import spp.cli.Main
import spp.protocol.instrument.LiveBreakpoint
import spp.protocol.instrument.LiveLog
import spp.protocol.instrument.LiveMeter
import spp.protocol.instrument.LiveSpan
import java.io.OutputStream
import java.io.PrintStream
import kotlin.reflect.KClass

abstract class CLIIntegrationTest {
    companion object {
        init {
            DatabindCodec.mapper().registerModule(KotlinModule())

            Main.standalone = false
            Main.main(
                arrayOf(
                    "-v",
                    "admin",
                    "reset"
                )
            )
        }
    }

    class Interceptor(out: OutputStream) : PrintStream(out, true) {
        val output = StringBuilder()

        override fun print(s: String) {
            output.append(s)
        }

        fun clear() {
            output.clear()
        }
    }

    fun <T : Any> toList(jsonString: String, clazz: KClass<T>): MutableList<T> {
        val value = Json.decodeValue(jsonString) as JsonArray
        val list = mutableListOf<T>()
        for (it in value.withIndex()) {
            val v = value.getJsonObject(it.index)
            if (v.getString("type") == "BREAKPOINT") {
                list.add(v.mapTo(LiveBreakpoint::class.java) as T)
            } else if (v.getString("type") == "LOG") {
                list.add(v.mapTo(LiveLog::class.java) as T)
            } else if (v.getString("type") == "METER") {
                list.add(v.mapTo(LiveMeter::class.java) as T)
            } else if (v.getString("type") == "SPAN") {
                list.add(v.mapTo(LiveSpan::class.java) as T)
            } else {
                list.add(v.mapTo(clazz.java) as T)
            }
        }
        return list
    }
}
