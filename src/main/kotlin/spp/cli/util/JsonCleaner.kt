/*
 * Source++, the continuous feedback platform for developers.
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
