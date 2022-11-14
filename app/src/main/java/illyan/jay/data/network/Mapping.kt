/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.network

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import illyan.jay.data.network.model.PathDocument
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.toGeoPoint
import illyan.jay.util.toTimestamp
import illyan.jay.util.toZonedDateTime
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

fun DomainSession.toHashMap() = hashMapOf(
    "uuid" to uuid,
    "distance" to distance,
    "endDateTime" to endDateTime?.toTimestamp(),
    "endLocation" to endLocation?.toGeoPoint(),
    "endLocationName" to endLocationName,
    "startDateTime" to startDateTime.toTimestamp(),
    "startLocation" to startLocation?.toGeoPoint(),
    "startLocationName" to startLocationName,
    "clientUUID" to clientUUID
)

// TODO: Limit size to 1MiB per hashMap
fun List<DomainLocation>.toPaths(
    sessionUUID: String
): List<PathDocument> {

    val accuracyChangeTimestamps = mutableListOf<Timestamp>()
    val accuracyChanges = mutableListOf<Byte>()
    val altitudes = mutableListOf<Short>()
    val bearingAccuracyChangeTimestamps = mutableListOf<Timestamp>()
    val bearingAccuracyChanges = mutableListOf<Short>()
    val bearings = mutableListOf<Short>()
    val coords = mutableListOf<GeoPoint>()
    val speeds = mutableListOf<Float>()
    val speedAccuracyChangeTimestamps = mutableListOf<Timestamp>()
    val speedAccuracyChanges = mutableListOf<Float>()
    val timestamps = mutableListOf<Timestamp>()
    val verticalAccuracyChangeTimestamps = mutableListOf<Timestamp>()
    val verticalAccuracyChanges = mutableListOf<Short>()

    val sortedLocations = sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }

    sortedLocations.forEach {
        val timestamp = it.zonedDateTime.toTimestamp()

        altitudes.add(it.altitude)
        bearings.add(it.bearing)
        coords.add(it.latLng.toGeoPoint())
        speeds.add(it.speed)
        timestamps.add(timestamp)

        val lastAccuracyChange = accuracyChanges.lastOrNull()
        if (lastAccuracyChange != it.accuracy) {
            accuracyChangeTimestamps.add(timestamp)
            accuracyChanges.add(it.accuracy)
        }

        val lastBearingAccuracyChange = bearingAccuracyChanges.lastOrNull()
        if (lastBearingAccuracyChange != it.bearingAccuracy) {
            bearingAccuracyChangeTimestamps.add(timestamp)
            bearingAccuracyChanges.add(it.bearingAccuracy)
        }

        val lastSpeedAccuracyChange = speedAccuracyChanges.lastOrNull()
        if (lastSpeedAccuracyChange != it.speedAccuracy) {
            speedAccuracyChangeTimestamps.add(timestamp)
            speedAccuracyChanges.add(it.speedAccuracy)
        }

        val lastVerticalAccuracyChange = verticalAccuracyChanges.lastOrNull()
        if (lastVerticalAccuracyChange != it.verticalAccuracy) {
            verticalAccuracyChangeTimestamps.add(timestamp)
            verticalAccuracyChanges.add(it.verticalAccuracy)
        }
    }

    return listOf(
        PathDocument(
            uuid = UUID.randomUUID().toString(),
            accuracyChangeTimestamps = accuracyChangeTimestamps.toList(),
            accuracyChanges = accuracyChanges.toList(),
            altitudes = altitudes.toList(),
            bearingAccuracyChangeTimestamps = bearingAccuracyChangeTimestamps.toList(),
            bearingAccuracyChanges = bearingAccuracyChanges.toList(),
            bearings = bearings.toList(),
            coords = coords.toList(),
            sessionUUID = sessionUUID,
            speeds = speeds.toList(),
            speedAccuracyChangeTimestamps = speedAccuracyChangeTimestamps.toList(),
            speedAccuracyChanges = speedAccuracyChanges.toList(),
            timestamps = timestamps.toList(),
            verticalAccuracyChangeTimestamps = verticalAccuracyChangeTimestamps.toList(),
            verticalAccuracyChanges = verticalAccuracyChanges.toList(),
        )
    )
}

fun PathDocument.toHashMap() = hashMapOf(
    "uuid" to uuid,
    "accuracyChangeTimestamps" to accuracyChangeTimestamps,
    "accuracyChanges" to accuracyChanges.map { it.toInt() },
    "altitudes" to altitudes.map { it.toInt() },
    "bearingAccuracyChangeTimestamps" to bearingAccuracyChangeTimestamps,
    "bearingAccuracyChanges" to bearingAccuracyChanges.map { it.toInt() },
    "bearings" to bearings.map { it.toInt() },
    "coords" to coords,
    "sessionUUID" to sessionUUID,
    "speeds" to speeds,
    "speedAccuracyChangeTimestamps" to speedAccuracyChangeTimestamps,
    "speedAccuracyChanges" to speedAccuracyChanges,
    "timestamps" to timestamps,
    "verticalAccuracyChangeTimestamps" to verticalAccuracyChangeTimestamps,
    "verticalAccuracyChanges" to verticalAccuracyChanges.map { it.toInt() }
)

fun Map<String, Any?>.toDomainSession(
    uuid: String,
    userUUID: String
): DomainSession {
    val startLocation = this["startLocation"] as GeoPoint?
    val endLocation = this["endLocation"] as GeoPoint?
    return DomainSession(
        uuid = uuid,
        startDateTime = (this["startDateTime"] as Timestamp?)?.toZonedDateTime() ?: ZonedDateTime
            .ofInstant(Instant.EPOCH, ZoneOffset.UTC),
        endDateTime = (this["endDateTime"] as Timestamp?)?.toZonedDateTime(),
        startLocationLatitude = startLocation?.latitude?.toFloat(),
        startLocationLongitude = startLocation?.longitude?.toFloat(),
        endLocationLatitude = endLocation?.latitude?.toFloat(),
        endLocationLongitude = endLocation?.longitude?.toFloat(),
        startLocationName = this["startLocationName"] as String?,
        endLocationName = this["endLocationName"] as String?,
        distance = (this["distance"] as Double?)?.toFloat(),
        clientUUID = (this["clientUUID"] as String?),
        ownerUserUUID = userUUID,
        isSynced = true
    )
}

fun List<DocumentSnapshot>.toDomainLocations(): List<DomainLocation> {
    val domainLocations = mutableListOf<DomainLocation>()

    forEach { document ->
        val accuracyChangeTimestamps = document.get("accuracyChangeTimestamps") as List<Timestamp>
        val accuracyChanges = document.get("accuracyChanges") as List<Int>
        val altitudes = document.get("altitudes") as List<Int>
        val bearingAccuracyChangeTimestamps = document.get("bearingAccuracyChangeTimestamps") as List<Timestamp>
        val bearingAccuracyChanges = document.get("bearingAccuracyChanges") as List<Int>
        val bearings = document.get("bearings") as List<Int>
        val coords = document.get("coords") as List<GeoPoint>
        val speeds = document.get("speeds") as List<Float>
        val speedAccuracyChangeTimestamps = document.get("speedAccuracyChangeTimestamps") as List<Timestamp>
        val speedAccuracyChanges = document.get("speedAccuracyChanges") as List<Float>
        val timestamps = document.get("timestamps") as List<Timestamp>
        val verticalAccuracyChangeTimestamps = document.get("verticalAccuracyChangeTimestamps") as List<Timestamp>
        val verticalAccuracyChanges = document.get("verticalAccuracyChanges") as List<Int>
        val sessionUUID = document.getString("sessionId")

        timestamps.forEachIndexed { index, timestamp ->
            val indexOfLastAccuracyChange = accuracyChangeTimestamps.indexOfLast {
                it.toZonedDateTime().isBefore(timestamp.toZonedDateTime())
            }.coerceAtLeast(0)
            val indexOfLastBearingAccuracyChange = bearingAccuracyChangeTimestamps.indexOfLast {
                it.toZonedDateTime().isBefore(timestamp.toZonedDateTime())
            }.coerceAtLeast(0)
            val indexOfLastVerticalAccuracyChange = verticalAccuracyChangeTimestamps.indexOfLast {
                it.toZonedDateTime().isBefore(timestamp.toZonedDateTime())
            }.coerceAtLeast(0)
            val indexOfLastSpeedAccuracyChange = speedAccuracyChangeTimestamps.indexOfLast {
                it.toZonedDateTime().isBefore(timestamp.toZonedDateTime())
            }.coerceAtLeast(0)

            domainLocations.add(
                DomainLocation( // FIXME: save speed accuracy next time?
                    sessionUUID = sessionUUID ?: UUID.randomUUID().toString(),
                    zonedDateTime = timestamp.toZonedDateTime(),
                    latitude = coords[index].latitude.toFloat(),
                    longitude = coords[index].longitude.toFloat(),
                    speed = speeds[index],
                    speedAccuracy = speedAccuracyChanges[indexOfLastSpeedAccuracyChange],
                    accuracy = accuracyChanges[indexOfLastAccuracyChange].toByte(),
                    bearing = bearings[index].toShort(),
                    bearingAccuracy = bearingAccuracyChanges[indexOfLastBearingAccuracyChange].toShort(),
                    altitude = altitudes[index].toShort(),
                    verticalAccuracy = verticalAccuracyChanges[indexOfLastVerticalAccuracyChange].toShort()
                )
            )
        }
    }

    return domainLocations.sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
}