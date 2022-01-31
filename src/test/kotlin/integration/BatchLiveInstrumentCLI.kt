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

import io.vertx.core.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import spp.cli.Main
import spp.protocol.instrument.LiveInstrument
import spp.protocol.instrument.breakpoint.LiveBreakpoint

class BatchLiveInstrumentCLI : CLIIntegrationTest() {

    @Test
    fun create100LiveBreakpoints() {
        val origOut = System.out
        val interceptor = Interceptor(origOut)
        System.setOut(interceptor)

        //100 live bps
        val addedLiveBps = mutableListOf<LiveInstrument>()
        for (i in 0..99) {
            Main.main(
                arrayOf(
                    "-v",
                    "add-breakpoint",
                    "integration.BatchLiveInstrumentCLI", i.toString(),
                )
            )
            val addedLiveBp = Json.decodeValue(interceptor.output.toString(), LiveBreakpoint::class.java)
            addedLiveBps.add(addedLiveBp)
            assertNotNull(addedLiveBp.id)
            interceptor.clear()
        }

        //get live instruments
        Main.main(
            arrayOf(
                "-v",
                "get-instruments"
            )
        )

        val liveInstruments = toList(interceptor.output.toString(), LiveInstrument::class)
        assertEquals(100, liveInstruments.size)
        interceptor.clear()

        //todo: need clear-instruments method
        addedLiveBps.forEach {
            //remove live instrument
            Main.main(
                arrayOf(
                    "-v",
                    "remove-instrument",
                    it.id!!
                )
            )
            val removedLiveBp = Json.decodeValue(interceptor.output.toString(), LiveBreakpoint::class.java)
            assertEquals(it.id, removedLiveBp.id)
            interceptor.clear()
        }
    }
}
