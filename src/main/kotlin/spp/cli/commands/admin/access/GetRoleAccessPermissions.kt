package spp.cli.commands.admin.access

import access.GetRoleAccessPermissionsQuery
import com.apollographql.apollo.api.ScalarTypeAdapters
import com.apollographql.apollo.api.internal.SimpleResponseWriter
import com.apollographql.apollo.coroutines.await
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI
import spp.cli.PlatformCLI.echoError
import spp.cli.util.JsonCleaner.cleanJson
import kotlin.system.exitProcess

class GetRoleAccessPermissions : CliktCommand(printHelpOnEmptyArgs = true) {

    val role by argument(help = "Role name")

    override fun run() = runBlocking {
        val response = try {
            PlatformCLI.apolloClient.query(GetRoleAccessPermissionsQuery(role)).await()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(response.data!!.roleAccessPermissions.map {
            val respWriter = SimpleResponseWriter(ScalarTypeAdapters.DEFAULT)
            it.marshaller().marshal(respWriter)
            cleanJson(JsonObject(respWriter.toJson("")).getJsonObject("data")).encodePrettily()
        })
        if (Main.standalone) exitProcess(0)
    }
}
