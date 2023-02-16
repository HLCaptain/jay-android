/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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
import illyan.jay.data.network.model.FirestorePath
import illyan.jay.data.network.model.FirestoreSession
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.toGeoPoint
import illyan.jay.util.toTimestamp
import illyan.jay.util.toZonedDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

fun DomainSession.toFirestoreModel() = FirestoreSession(
    uuid = uuid,
    startDateTime = startDateTime.toTimestamp(),
    endDateTime = endDateTime?.toTimestamp(),
    startLocationName = startLocationName,
    endLocationName = endLocationName,
    clientUUID = clientUUID,
    distance = distance,
    startLocation = startLocation?.toGeoPoint(),
    endLocation = endLocation?.toGeoPoint(),
)

fun FirestoreSession.toDomainModel(
    ownerUUID: String
) = DomainSession(
    uuid = uuid,
    startDateTime = startDateTime.toZonedDateTime(),
    endDateTime = endDateTime?.toZonedDateTime(),
    startLocationName = startLocationName,
    endLocationName = endLocationName,
    clientUUID = clientUUID,
    ownerUUID = ownerUUID,
    distance = distance,
    startLocationLongitude = startLocation?.longitude?.toFloat(),
    startLocationLatitude = startLocation?.latitude?.toFloat(),
    endLocationLongitude = endLocation?.longitude?.toFloat(),
    endLocationLatitude = endLocation?.latitude?.toFloat(),
)

fun List<DomainLocation>.toPath(
    sessionUUID: String,
    ownerUUID: String
): FirestorePath {
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

    return FirestorePath(
        uuid = UUID.randomUUID().toString(),
        sessionUUID = sessionUUID,
        ownerUUID = ownerUUID,
        accuracyChangeTimestamps = accuracyChangeTimestamps,
        accuracyChanges = accuracyChanges,
        altitudes = altitudes,
        bearingAccuracyChangeTimestamps = bearingAccuracyChangeTimestamps,
        bearingAccuracyChanges = bearingAccuracyChanges,
        bearings = bearings,
        coords = coords,
        speeds = speeds,
        speedAccuracyChangeTimestamps = speedAccuracyChangeTimestamps,
        speedAccuracyChanges = speedAccuracyChanges,
        timestamps = timestamps,
        verticalAccuracyChangeTimestamps = verticalAccuracyChangeTimestamps,
        verticalAccuracyChanges = verticalAccuracyChanges,
    )
}

fun List<DomainLocation>.toPaths(
    sessionUUID: String,
    ownerUUID: String,
    thresholdInMinutes: Int = 30
): List<FirestorePath> {
    if (isEmpty()) return emptyList()
    val startMilli = minOf { it.zonedDateTime.toInstant().toEpochMilli() }
    val groupedByTime = groupBy {(it.zonedDateTime.toInstant().toEpochMilli() - startMilli) / thresholdInMinutes.minutes.inWholeMilliseconds }
    return groupedByTime.map {
        it.value.toPath(sessionUUID, ownerUUID)
    }
}

fun FirestorePath.toHashMap() = hashMapOf(
    "uuid" to uuid,
    "sessionUUID" to sessionUUID,
    "ownerUUID" to ownerUUID,
    "accuracyChangeTimestamps" to accuracyChangeTimestamps,
    "accuracyChanges" to accuracyChanges.map { it.toInt() },
    "altitudes" to altitudes.map { it.toInt() },
    "bearingAccuracyChangeTimestamps" to bearingAccuracyChangeTimestamps,
    "bearingAccuracyChanges" to bearingAccuracyChanges.map { it.toInt() },
    "bearings" to bearings.map { it.toInt() },
    "coords" to coords,
    "speeds" to speeds,
    "speedAccuracyChangeTimestamps" to speedAccuracyChangeTimestamps,
    "speedAccuracyChanges" to speedAccuracyChanges,
    "timestamps" to timestamps,
    "verticalAccuracyChangeTimestamps" to verticalAccuracyChangeTimestamps,
    "verticalAccuracyChanges" to verticalAccuracyChanges.map { it.toInt() }
)

fun List<DocumentSnapshot>.toDomainLocations(): List<DomainLocation> {
    val domainLocations = mutableListOf<DomainLocation>()

    forEach { document ->
        val timestamps = (document.get("timestamps") as? List<Timestamp> ?: emptyList()).map { it.toZonedDateTime() }
        val sessionUUID = document.getString("sessionUUID") ?: ""
        val accuracyChangeTimestamps = (document.get("accuracyChangeTimestamps") as? List<Timestamp> ?: emptyList()).map { it.toZonedDateTime() }
        val accuracyChanges = document.get("accuracyChanges") as? List<Int> ?: emptyList()
        val altitudes = document.get("altitudes") as? List<Int> ?: emptyList()
        val bearingAccuracyChangeTimestamps = (document.get("bearingAccuracyChangeTimestamps") as? List<Timestamp> ?: emptyList()).map { it.toZonedDateTime() }
        val bearingAccuracyChanges = document.get("bearingAccuracyChanges") as? List<Int> ?: emptyList()
        val bearings = document.get("bearings") as? List<Int> ?: emptyList()
        val coords = document.get("coords") as? List<GeoPoint> ?: emptyList()
        val speeds = document.get("speeds") as? List<Float> ?: emptyList()
        val speedAccuracyChangeTimestamps = (document.get("speedAccuracyChangeTimestamps") as? List<Timestamp> ?: emptyList()).map { it.toZonedDateTime() }
        val speedAccuracyChanges = document.get("speedAccuracyChanges") as? List<Float> ?: emptyList()
        val verticalAccuracyChangeTimestamps = (document.get("verticalAccuracyChangeTimestamps") as? List<Timestamp> ?: emptyList()).map { it.toZonedDateTime() }
        val verticalAccuracyChanges = document.get("verticalAccuracyChanges") as? List<Int> ?: emptyList()

        timestamps.forEachIndexed { index, zonedDateTime ->
            val indexOfLastAccuracyChange = accuracyChangeTimestamps
                .indexOfLast {
                it.isBefore(zonedDateTime)
            }.coerceAtLeast(0)
            val indexOfLastBearingAccuracyChange = bearingAccuracyChangeTimestamps
                .indexOfLast {
                it.isBefore(zonedDateTime)
            }.coerceAtLeast(0)
            val indexOfLastVerticalAccuracyChange = verticalAccuracyChangeTimestamps
                .indexOfLast {
                it.isBefore(zonedDateTime)
            }.coerceAtLeast(0)
            val indexOfLastSpeedAccuracyChange = speedAccuracyChangeTimestamps
                .indexOfLast {
                it.isBefore(zonedDateTime)
            }.coerceAtLeast(0)

            domainLocations.add(
                DomainLocation(
                    sessionUUID = sessionUUID,
                    zonedDateTime = zonedDateTime,
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