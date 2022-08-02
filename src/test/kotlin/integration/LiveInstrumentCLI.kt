/*
 * Source++, the open-source live coding platform.
 * Copyright (C) 2022 CodeBrig, Inc.
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import spp.cli.Main
import spp.protocol.instrument.LiveBreakpoint
import spp.protocol.instrument.LiveInstrument
import spp.protocol.instrument.LiveLog

class LiveInstrumentCLI : CLIIntegrationTest() {

    @Test
    fun addRemoveLiveLog() {
        val origOut = System.out
        val interceptor = Interceptor(origOut)
        System.setOut(interceptor)

        //add live log
        Main.main(
            arrayOf(
                "-v",
                "add", "log",
                "-h", "100",
                "integration.LiveInstrumentCLI",
                "1",
                "addRemoveLiveLog"
            )
        )
        val addedLiveLog = Json.decodeValue(interceptor.output.toString(), LiveLog::class.java)
        assertNotNull(addedLiveLog.id)
        assertEquals("addRemoveLiveLog", addedLiveLog.logFormat)
        assertEquals("integration.LiveInstrumentCLI", addedLiveLog.location.source)
        assertEquals(1, addedLiveLog.location.line)
        assertEquals(100, addedLiveLog.hitLimit)

        interceptor.clear()

        //remove live instrument
        Main.main(
            arrayOf(
                "-v",
                "remove", "instrument",
                addedLiveLog.id!!
            )
        )
        val removedLiveLog = Json.decodeValue(interceptor.output.toString(), LiveLog::class.java)
        assertEquals(addedLiveLog.id, removedLiveLog.id)
        assertEquals(addedLiveLog.logFormat, removedLiveLog.logFormat)
        assertEquals(addedLiveLog.location.source, removedLiveLog.location.source)
        assertEquals(addedLiveLog.location.line, removedLiveLog.location.line)
        assertEquals(addedLiveLog.hitLimit, removedLiveLog.hitLimit)
    }

    @Test
    fun addRemoveLiveBreakpoint() {
        val origOut = System.out
        val interceptor = Interceptor(origOut)
        System.setOut(interceptor)

        //add live breakpoint
        Main.main(
            arrayOf(
                "-v",
                "add", "breakpoint",
                "integration.LiveInstrumentCLI", "2",
            )
        )
        val removedLiveBp = Json.decodeValue(interceptor.output.toString(), LiveBreakpoint::class.java)
        assertNotNull(removedLiveBp.id)
        assertEquals("integration.LiveInstrumentCLI", removedLiveBp.location.source)
        assertEquals(2, removedLiveBp.location.line)
        assertEquals(1, removedLiveBp.hitLimit)

        interceptor.clear()

        //remove live instrument
        Main.main(
            arrayOf(
                "-v",
                "remove", "instrument",
                removedLiveBp.id!!
            )
        )
        val removedLiveBreakpoint = Json.decodeValue(interceptor.output.toString(), LiveBreakpoint::class.java)
        assertEquals(removedLiveBp.id, removedLiveBreakpoint.id)
        assertEquals(removedLiveBp.location.source, removedLiveBreakpoint.location.source)
        assertEquals(removedLiveBp.location.line, removedLiveBreakpoint.location.line)
        assertEquals(removedLiveBp.hitLimit, removedLiveBreakpoint.hitLimit)
    }

    @Test
    fun getMultipleLiveInstruments() {
        val origOut = System.out
        val interceptor = Interceptor(origOut)
        System.setOut(interceptor)

        //add live log
        Main.main(
            arrayOf(
                "-v",
                "add", "log",
                "integration.LiveInstrumentCLI", "4",
                "getMultipleLiveInstruments"
            )
        )
        val addedLiveLog = Json.decodeValue(interceptor.output.toString(), LiveLog::class.java)
        assertNotNull(addedLiveLog.id)
        assertEquals("getMultipleLiveInstruments", addedLiveLog.logFormat)
        assertEquals("integration.LiveInstrumentCLI", addedLiveLog.location.source)
        assertEquals(4, addedLiveLog.location.line)
        interceptor.clear()

        //add live breakpoint
        Main.main(
            arrayOf(
                "-v",
                "add", "breakpoint",
                "integration.LiveInstrumentCLI", "4",
            )
        )
        val addedLiveBp = Json.decodeValue(interceptor.output.toString(), LiveBreakpoint::class.java)
        assertNotNull(addedLiveBp.id)
        assertEquals("integration.LiveInstrumentCLI", addedLiveBp.location.source)
        assertEquals(4, addedLiveBp.location.line)
        interceptor.clear()

        //get live instruments
        Main.main(
            arrayOf(
                "-v",
                "get", "instruments"
            )
        )
        val liveInstruments = toList(interceptor.output.toString(), LiveInstrument::class)
        assertEquals(2, liveInstruments.size)
        assertTrue(liveInstruments.any { it.id == addedLiveBp.id })
        assertTrue(liveInstruments.any { it.id == addedLiveLog.id })
        interceptor.clear()

        //remove live log
        Main.main(
            arrayOf(
                "-v",
                "remove", "instrument",
                addedLiveLog.id!!
            )
        )
        val removedLiveLog = Json.decodeValue(interceptor.output.toString(), LiveLog::class.java)
        assertEquals(addedLiveLog.id, removedLiveLog.id)
        assertEquals(addedLiveLog.location.source, removedLiveLog.location.source)
        assertEquals(addedLiveLog.location.line, removedLiveLog.location.line)
        interceptor.clear()

        //remove live breakpoint
        Main.main(
            arrayOf(
                "-v",
                "remove", "instrument",
                addedLiveBp.id!!
            )
        )
        val removedLiveBp = Json.decodeValue(interceptor.output.toString(), LiveBreakpoint::class.java)
        assertEquals(addedLiveBp.id, removedLiveBp.id)
        assertEquals(addedLiveBp.location.source, removedLiveBp.location.source)
        assertEquals(addedLiveBp.location.line, removedLiveBp.location.line)
    }
}
