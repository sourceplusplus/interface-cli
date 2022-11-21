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
package spp.cli.commands.developer.instrument

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.json.MapJsonWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.coroutines.runBlocking
import spp.cli.PlatformCLI.apolloClient
import spp.cli.protocol.instrument.AddLiveMeterMutation
import spp.cli.protocol.instrument.adapter.AddLiveMeterMutation_ResponseAdapter.AddLiveMeter
import spp.cli.protocol.type.InstrumentThrottleInput
import spp.cli.protocol.type.LiveMeterInput
import spp.cli.protocol.type.LiveSourceLocationInput
import spp.cli.protocol.type.MetricValueInput
import spp.cli.util.ExitManager.exitProcess
import spp.cli.util.JsonCleaner
import spp.protocol.instrument.meter.MeterType
import spp.protocol.instrument.meter.MetricValueType
import spp.protocol.instrument.throttle.ThrottleStep

class AddMeter : CliktCommand(name = "meter", help = "Add a live meter instrument") {

    val source by argument(help = "Qualified class name")
    val line by argument(help = "Line number").int()
    val meterType by argument(help = "Meter type").enum<MeterType>()
    val valueType by argument(help = "Metric value type").enum<MetricValueType>()
    val value by option("-value", "-v", help = "Metric value")
    val meterId by option("-id", help = "Meter identifier")
    val condition by option("-condition", "-c", help = "Trigger condition")
    val expiresAt by option("-expiresAt", "-e", help = "Expiration time (epoch time [ms])").long()
    val hitLimit by option("-hitLimit", "-h", help = "Trigger hit limit").int()
    val throttleLimit by option("-throttleLimit", "-t", help = "Trigger throttle limit").int().default(1)
    val throttleStep by option("-throttleStep", "-s", help = "Trigger throttle step").enum<ThrottleStep>()
        .default(ThrottleStep.SECOND)

    override fun run() = runBlocking {
        val input = LiveMeterInput(
            meterType = spp.cli.protocol.type.MeterType.valueOf(meterType.toString()),
            metricValue = MetricValueInput(
                valueType = spp.cli.protocol.type.MetricValueType.valueOf(valueType.toString()),
                value = Optional.presentIfNotNull(value)
            ),
            id = Optional.presentIfNotNull(meterId),
            location = LiveSourceLocationInput(source, line),
            condition = Optional.Present(condition),
            expiresAt = Optional.Present(expiresAt),
            hitLimit = Optional.Present(hitLimit),
            throttle = Optional.Present(
                InstrumentThrottleInput(
                    throttleLimit, spp.cli.protocol.type.ThrottleStep.valueOf(throttleStep.toString())
                )
            )
        )
        val response = try {
            apolloClient.mutation(AddLiveMeterMutation(input)).execute()
        } catch (e: Exception) {
            exitProcess(-1, e)
        }
        if (response.hasErrors()) {
            exitProcess(response.errors!!)
        }

        echo(JsonCleaner.cleanJson(MapJsonWriter().let {
            it.beginObject()
            AddLiveMeter.toJson(it, CustomScalarAdapters.Empty, response.data!!.addLiveMeter)
            it.endObject()
            (it.root() as LinkedHashMap<*, *>)
        }).encodePrettily())
        exitProcess(0)
    }
}
