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
package spp.cli.util

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

@Suppress("UNCHECKED_CAST")
object JsonCleaner {

    fun cleanJson(any: Any?): JsonObject {
        val ob = JsonObject.mapFrom(any) ?: JsonObject()
        if (ob.getValue("onLiveLog") != null) {
            val innerOb = ob.getJsonObject("onLiveLog")
            innerOb.fieldNames().forEach {
                ob.put(it, innerOb.getValue(it))
            }
        }
        ob.remove("onLiveLog")
        ob.remove("__typename")
        return cleanJson(ob)
    }

    fun cleanJson(list: List<Any>): JsonArray {
        val arr = JsonArray()
        list.forEach {
            arr.add(cleanJson(it))
        }
        return arr
    }

    fun cleanJson(json: JsonObject): JsonObject {
        val cleanJson = JsonObject()
        json.fieldNames().forEach {
            when (val value = json.getValue(it)) {
                is JsonObject -> cleanJson.put(it, cleanJson(value))
                is JsonArray -> cleanJson.put(it, cleanJsonArray(value))
                else -> cleanJson.put(it, value)
            }
        }

        //reformat meta
        if (cleanJson.containsKey("meta") && cleanJson.getValue("meta") is JsonArray) {
            val metaArr = cleanJson.getJsonArray("meta")
            val metaOb = JsonObject()
            for (i in 0 until metaArr.size()) {
                val entry = metaArr.getJsonObject(i)
                metaOb.put(entry.getString("name"), entry.getValue("value"))
            }
            cleanJson.put("meta", metaOb)
        }

        return cleanJson
    }

    private fun cleanJsonArray(jsonArray: JsonArray): JsonArray {
        val cleanJsonArray = JsonArray()
        for (i in 0 until jsonArray.size()) {
            when (val value = jsonArray.getValue(i)) {
                is JsonObject -> cleanJsonArray.add(cleanJson(value))
                is JsonArray -> cleanJsonArray.add(cleanJsonArray(value))
                is String -> cleanJsonArray.add(value)
                else -> throw UnsupportedOperationException("Type: " + value.javaClass.simpleName)
            }
        }
        return cleanJsonArray
    }
}
