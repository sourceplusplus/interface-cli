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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import spp.cli.Main
import spp.protocol.instrument.LiveBreakpoint
import spp.protocol.instrument.LiveInstrument

class BatchLiveInstrumentCLI : CLIIntegrationTest() {

    @Test
    fun create100LiveBreakpoints() {
        val origOut = System.out
        val interceptor = Interceptor(origOut)
        System.setOut(interceptor)

        //100 live bps
        val addedLiveBps = mutableListOf<LiveInstrument>()
        for (i in 0..99) {
            log.info("Adding live breakpoint: bp-$i")
            Main.main(
                arrayOf(
                    "-v",
                    "add", "breakpoint",
                    "-i", "bp-$i",
                    "-l", i.toString(),
                    "integration.BatchLiveInstrumentCLI",
                )
            )
            val addedLiveBp = LiveBreakpoint(JsonObject(interceptor.output.toString()))
            addedLiveBps.add(addedLiveBp)
            assertNotNull(addedLiveBp.id)
            interceptor.clear()
            log.info("Added live breakpoint: ${addedLiveBp.id}")
        }

        //get live bps
        Main.main(
            arrayOf(
                "-v",
                "get", "instruments"
            )
        )

        val liveInstruments = toList(interceptor.output.toString(), LiveInstrument::class)
        for (i in 0..99) {
            assertNotNull(liveInstruments.find { it.id == "bp-$i" })
        }
        interceptor.clear()

        //remove live bps
        addedLiveBps.forEach {
            Main.main(
                arrayOf(
                    "-v",
                    "remove", "instrument",
                    it.id!!
                )
            )
            val removedLiveBp = LiveBreakpoint(JsonObject(interceptor.output.toString()))
            assertEquals(it.id, removedLiveBp.id)
            interceptor.clear()
        }
        System.setOut(origOut)
    }
}
