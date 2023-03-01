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

import android.os.Parcel
import android.os.Parcelable
import com.github.luben.zstd.Zstd
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import illyan.jay.BuildConfig
import illyan.jay.data.network.model.FirestorePath
import illyan.jay.data.network.model.FirestoreSession
import illyan.jay.data.network.model.FirestoreUserPreferences
import illyan.jay.data.network.serializers.TimestampSerializer
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainPreferences
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.toGeoPoint
import illyan.jay.util.toTimestamp
import illyan.jay.util.toZonedDateTime
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream
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

fun FirestoreUserPreferences.toDomainModel(
    userUUID: String
) = DomainPreferences(
    userUUID = userUUID,
    analyticsEnabled = analyticsEnabled,
    freeDriveAutoStart = freeDriveAutoStart,
    lastUpdate = lastUpdate.toZonedDateTime(),
    shouldSync = true
)

fun DomainPreferences.toFirestoreModel() = FirestoreUserPreferences(
    analyticsEnabled = analyticsEnabled,
    freeDriveAutoStart = freeDriveAutoStart,
    lastUpdate = lastUpdate.toTimestamp(),
)

@OptIn(ExperimentalSerializationApi::class)
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

    val path = FirestorePath(
        uuid = UUID.randomUUID().toString(),
        sessionUUID = sessionUUID,
        ownerUUID = ownerUUID,
        accuracyChangeTimestamps = accuracyChangeTimestamps,
        accuracyChanges = accuracyChanges.map { it.toInt() },
        altitudes = altitudes.map { it.toInt() },
        bearingAccuracyChangeTimestamps = bearingAccuracyChangeTimestamps,
        bearingAccuracyChanges = bearingAccuracyChanges.map { it.toInt() },
        bearings = bearings.map { it.toInt() },
        coords = coords,
        speeds = speeds,
        speedAccuracyChangeTimestamps = speedAccuracyChangeTimestamps,
        speedAccuracyChanges = speedAccuracyChanges,
        timestamps = timestamps,
        verticalAccuracyChangeTimestamps = verticalAccuracyChangeTimestamps,
        verticalAccuracyChanges = verticalAccuracyChanges.map { it.toInt() },
    )

    if (BuildConfig.DEBUG) {
        // Size comparison between raw location data path and compressed data structures.

        // Conclusion: Path reduces data size ~13% compared to raw location data with SessionUUID
        // and optimized sensory data (Short, Byte used instead of Int).
        // Raw, optimized location data without SessionUUID, is taking up ~49% less space
        // compared to raw location data with SessionUUID (repeated).

        // Size ratio:
        // - List<DomainLocations> = 1.00
        // - FirestorePath = ~0.87
        // - List<LocationWithoutSessionIdOptimized> = ~0.51
        // - List<LocationWithoutSessionId> = ~0.51
        // - Base64 GZIP compressed List<LocationWithoutSessionIdOptimized> = ~0.16
        // - Base64 ZLIB compressed List<LocationWithoutSessionIdOptimized> = ~0.17

        val optimizedLocations = map {
            LocationWithoutSessionIdOptimized(
                zonedDateTime = it.zonedDateTime.toTimestamp(),
                latitude = it.latitude,
                longitude = it.longitude,
                speed = it.speed,
                accuracy = it.accuracy,
                bearing = it.bearing,
                bearingAccuracy = it.bearingAccuracy,
                altitude = it.altitude,
                speedAccuracy = it.speedAccuracy,
                verticalAccuracy = it.verticalAccuracy
            )
        }

        val locations = map {
            LocationWithoutSessionId(
                zonedDateTime = it.zonedDateTime.toTimestamp(),
                latitude = it.latitude,
                longitude = it.longitude,
                speed = it.speed,
                accuracy = it.accuracy.toInt(),
                bearing = it.bearing.toInt(),
                bearingAccuracy = it.bearingAccuracy.toInt(),
                altitude = it.altitude.toInt(),
                speedAccuracy = it.speedAccuracy,
                verticalAccuracy = it.verticalAccuracy.toInt()
            )
        }

        val unoptimizedLocations = map {
            LocationWithoutSessionIdUnoptimized(
                zonedDateTime = it.zonedDateTime.toTimestamp(),
                latitude = it.latitude.toDouble(),
                longitude = it.longitude.toDouble(),
                speed = it.speed.toDouble(),
                accuracy = it.accuracy.toLong(),
                bearing = it.bearing.toLong(),
                bearingAccuracy = it.bearingAccuracy.toLong(),
                altitude = it.altitude.toLong(),
                speedAccuracy = it.speedAccuracy.toDouble(),
                verticalAccuracy = it.verticalAccuracy.toLong()
            )
        }

        val data = listOf(
            ProtoBuf.encodeToByteArray(optimizedLocations) to "Optimized Locations",
            ProtoBuf.encodeToByteArray(locations) to "Default Locations",
            ProtoBuf.encodeToByteArray(unoptimizedLocations) to "Unoptimized Locations"
        )

        val compressions = listOf<Pair<(ByteArray) -> ByteArray, String>>(
            { array: ByteArray ->
                val locationsParcel = Parcel.obtain()
                locationsParcel.writeByteArray(array)
                val bytes = locationsParcel.marshall()
                locationsParcel.recycle()
                bytes
            } to "Parcel marshall",
            { array: ByteArray ->
                val gzipByteArrayOutputStream = ByteArrayOutputStream()
                val gzipOutputStream = GZIPOutputStream(gzipByteArrayOutputStream)
                gzipOutputStream.write(array)
                gzipByteArrayOutputStream.toByteArray()
            } to "GZIP",
            { array: ByteArray ->
                val zlibByteArrayOutputStream = ByteArrayOutputStream()
                val deflater = Deflater(Deflater.BEST_COMPRESSION)
                val deflaterOutputStream = DeflaterOutputStream(zlibByteArrayOutputStream, deflater)
                deflaterOutputStream.write(array)
                zlibByteArrayOutputStream.toByteArray()
            } to "ZLIB",
            { array: ByteArray ->
                Zstd.compress(array, Zstd.maxCompressionLevel())
            } to "Zstd"
        )

        data.forEach {
            val byteArray = it.first
            val stringBuilder = StringBuilder()
            stringBuilder.append("Data: ${it.second}\n")

            compressions.forEach { algo ->
                val compressedBytes = algo.first(byteArray)
                val algoName = algo.second
                stringBuilder.append("$algoName: ${compressedBytes.size} bytes\n")
            }
            Timber.d(stringBuilder.toString())
        }
    }

    return path
}

@Serializable
@Parcelize
data class LocationWithoutSessionIdOptimized(
    @Serializable(with = TimestampSerializer::class)
    val zonedDateTime: Timestamp,
    val latitude: Float,
    val longitude: Float,
    var speed: Float = Float.MIN_VALUE,
    var accuracy: Byte = Byte.MIN_VALUE,
    var bearing: Short = Short.MIN_VALUE,
    var bearingAccuracy: Short = Short.MIN_VALUE, // in degrees
    var altitude: Short = Short.MIN_VALUE,
    var speedAccuracy: Float = Float.MIN_VALUE, // in meters per second
    var verticalAccuracy: Short = Short.MIN_VALUE, // in meters
) : Parcelable

@Serializable
@Parcelize
data class LocationWithoutSessionId(
    @Serializable(with = TimestampSerializer::class)
    val zonedDateTime: Timestamp,
    val latitude: Float,
    val longitude: Float,
    var speed: Float = Float.MIN_VALUE,
    var accuracy: Int = Int.MIN_VALUE,
    var bearing: Int = Int.MIN_VALUE,
    var bearingAccuracy: Int = Int.MIN_VALUE, // in degrees
    var altitude: Int = Int.MIN_VALUE,
    var speedAccuracy: Float = Float.MIN_VALUE, // in meters per second
    var verticalAccuracy: Int = Int.MIN_VALUE, // in meters
) : Parcelable

@Serializable
@Parcelize
data class LocationWithoutSessionIdUnoptimized(
    @Serializable(with = TimestampSerializer::class)
    val zonedDateTime: Timestamp,
    val latitude: Double,
    val longitude: Double,
    var speed: Double = Double.MIN_VALUE,
    var accuracy: Long = Long.MIN_VALUE,
    var bearing: Long = Long.MIN_VALUE,
    var bearingAccuracy: Long = Long.MIN_VALUE, // in degrees
    var altitude: Long = Long.MIN_VALUE,
    var speedAccuracy: Double = Double.MIN_VALUE, // in meters per second
    var verticalAccuracy: Long = Long.MIN_VALUE, // in meters
) : Parcelable

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