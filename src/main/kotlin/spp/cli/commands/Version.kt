package spp.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import java.util.*

class Version : CliktCommand() {
    private val BUILD = ResourceBundle.getBundle("build")

    override fun run() {
        echo("spp-cli: " + BUILD.getString("build_version"))
        echo("Build id: " + BUILD.getString("build_id"))
        echo("Build date: " + BUILD.getString("build_date"))
    }
}
