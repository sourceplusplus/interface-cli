package spp.cli.commands.admin.access

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.runBlocking
import spp.cli.Main
import spp.cli.PlatformCLI
import spp.cli.PlatformCLI.echoError
import spp.cli.protocol.access.GetRoleAccessPermissionsQuery
import spp.cli.protocol.access.adapter.GetRoleAccessPermissionsQuery_ResponseAdapter.GetRoleAccessPermission
import spp.cli.util.JsonCleaner
import kotlin.system.exitProcess

class GetRoleAccessPermissions : CliktCommand(printHelpOnEmptyArgs = true) {

    val role by argument(help = "Role name")

    override fun run() = runBlocking {
        val response = try {
            PlatformCLI.apolloClient.query(GetRoleAccessPermissionsQuery(role)).execute()
        } catch (e: Exception) {
            echoError(e)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }
        if (response.hasErrors()) {
            echo(response.errors?.get(0)?.message, err = true)
            if (Main.standalone) exitProcess(-1) else return@runBlocking
        }

        echo(JsonCleaner.cleanJson(MapJsonWriter().let {
            it.beginArray()
            response.data!!.getRoleAccessPermissions.forEach { ob ->
                it.beginObject()
                GetRoleAccessPermission.toJson(it, CustomScalarAdapters.Empty, ob)
                it.endObject()
            }
            it.endArray()
            (it.root() as ArrayList<*>)
        }).encodePrettily())
        if (Main.standalone) exitProcess(0)
    }
}
