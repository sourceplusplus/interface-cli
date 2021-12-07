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
