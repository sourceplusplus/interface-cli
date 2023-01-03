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

import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import spp.cli.Main
import spp.cli.util.ExitManager
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
            ExitManager.standalone = false
            Main.main(
                arrayOf(
                    "-v",
                    "-a", "change-me",
                    "admin",
                    "reset"
                )
            )
        }
    }

    class Interceptor(out: OutputStream) : PrintStream(out, true) {
        val output = StringBuilder()

        override fun print(s: String?) {
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
                list.add(LiveBreakpoint(v) as T)
            } else if (v.getString("type") == "LOG") {
                list.add(LiveLog(v) as T)
            } else if (v.getString("type") == "METER") {
                list.add(LiveMeter(v) as T)
            } else if (v.getString("type") == "SPAN") {
                list.add(LiveSpan(v) as T)
            } else {
                list.add(v.mapTo(clazz.java) as T)
            }
        }
        return list
    }
}
