/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.sensor

import androidx.compose.ui.util.lerp
import illyan.jay.domain.model.AdvancedImuSensorData
import illyan.jay.domain.model.DomainSensorEvent
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object SensorFusion {
    fun fuseSensorsWithInterval(
        interval: Duration = 10.milliseconds,
        accRaw: List<DomainSensorEvent>,
        accSmooth: List<DomainSensorEvent>,
        dirX: List<DomainSensorEvent>,
        dirY: List<DomainSensorEvent>,
        dirZ: List<DomainSensorEvent>,
        angVel: List<DomainSensorEvent>,
        angAccel: List<DomainSensorEvent>,
    ): List<AdvancedImuSensorData> {
        val allTimestamps = (accRaw + accSmooth + dirX + dirY + dirZ + angVel + angAccel)
            .map { it.zonedDateTime.toInstant().toEpochMilli() }
            .distinct()
            .sorted()

        return if (allTimestamps.isEmpty()) {
            Timber.d("No sensor data to fuse")
            emptyList()
        } else {
            val minTimestamp = allTimestamps.min()
            val maxTimestamp = allTimestamps.max()
            val intervals = (minTimestamp until maxTimestamp step interval.inWholeMilliseconds).toList()
            return fuseSensors(
                accRaw = accRaw,
                accSmooth = accSmooth,
                dirX = dirX,
                dirY = dirY,
                dirZ = dirZ,
                angVel = angVel,
                angAccel = angAccel,
                intervals = intervals
            )
        }
    }

    fun fuseSensors(
        accRaw: List<DomainSensorEvent>,
        accSmooth: List<DomainSensorEvent>,
        dirX: List<DomainSensorEvent>,
        dirY: List<DomainSensorEvent>,
        dirZ: List<DomainSensorEvent>,
        angVel: List<DomainSensorEvent>,
        angAccel: List<DomainSensorEvent>,
        intervals: List<Long> = (accRaw + accSmooth + dirX + dirY + dirZ + angVel + angAccel)
            .map { it.zonedDateTime.toInstant().toEpochMilli() }
            .distinct()
            .sorted()
    ): List<AdvancedImuSensorData> {
        Timber.d("Fusing sensor data")
        // Merge all timestamps

        if (intervals.isEmpty()) Timber.d("No sensor data to fuse")

        val interpolatedDirX = interpolateValues(dirX, intervals)
        val interpolatedDirY = interpolateValues(dirY, intervals)
        val interpolatedDirZ = interpolateValues(dirZ, intervals)
        val interpolatedAccRaw = interpolateValues(accRaw, intervals)
        val interpolatedAccSmooth = interpolateValues(accSmooth, intervals)
        val interpolatedAngVel = interpolateValues(angVel, intervals)
        val interpolatedAngAccel = interpolateValues(angAccel, intervals)

        return intervals.mapIndexed { index, timestamp ->
            Timber.v("Fusing sensor data for timestamp $timestamp (${index + 1}/${intervals.size})")
            // Interpolate values for each timestamp
            AdvancedImuSensorData(
                dirX = interpolatedDirX[index],
                dirY = interpolatedDirY[index],
                dirZ = interpolatedDirZ[index],
                accRaw = interpolatedAccRaw[index],
                accSmooth = interpolatedAccSmooth[index],
                angVel = interpolatedAngVel[index],
                angAccel = interpolatedAngAccel[index],
                timestamp = timestamp
            )
        }
    }

    private fun interpolateValues(events: List<DomainSensorEvent>, timestamps: List<Long>): List<Triple<Double, Double, Double>> {
        // Linear-like interpolation
        if (events.isEmpty()) return timestamps.map { Triple(0.0, 0.0, 0.0) }
        val firstEvent = events.first()
        val lastEvent = events.last()
        return timestamps.map {  timestamp ->
            val beforeEvent = events.firstOrNull { it.zonedDateTime.toInstant().toEpochMilli() <= timestamp } ?: firstEvent
            val afterEvent = events.firstOrNull { it.zonedDateTime.toInstant().toEpochMilli() >= timestamp } ?: lastEvent
            if (beforeEvent == afterEvent) return@map Triple(beforeEvent.x.toDouble(), beforeEvent.y.toDouble(), beforeEvent.z.toDouble())
            val fraction = (timestamp - beforeEvent.zonedDateTime.toInstant().toEpochMilli()).toFloat() /
                    (afterEvent.zonedDateTime.toInstant().toEpochMilli() - beforeEvent.zonedDateTime.toInstant().toEpochMilli()).toFloat()
            val interpolatedEventX = lerp(
                beforeEvent.x,
                afterEvent.x,
                fraction
            ).toDouble()
            val interpolatedEventY = lerp(
                beforeEvent.y,
                afterEvent.y,
                fraction
            ).toDouble()
            val interpolatedEventZ = lerp(
                beforeEvent.z,
                afterEvent.z,
                fraction
            ).toDouble()
            Triple(interpolatedEventX, interpolatedEventY, interpolatedEventZ)
        }
    }
}