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

package illyan.jay.data.firestore

import android.os.Parcel
import android.os.Parcelable
import com.github.luben.zstd.Zstd
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob
import illyan.jay.BuildConfig
import illyan.jay.data.DataStatus
import illyan.jay.data.firestore.model.FirestoreLocation
import illyan.jay.data.firestore.model.FirestorePath
import illyan.jay.data.firestore.model.FirestoreSensorEvent
import illyan.jay.data.firestore.model.FirestoreSensorEvents
import illyan.jay.data.firestore.model.FirestoreSession
import illyan.jay.data.firestore.model.FirestoreUser
import illyan.jay.data.firestore.model.FirestoreUserPreferences
import illyan.jay.data.firestore.serializers.TimestampSerializer
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainPreferences
import illyan.jay.domain.model.DomainSensorEvent
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.toGeoPoint
import illyan.jay.util.toInstant
import illyan.jay.util.toTimestamp
import illyan.jay.util.toZonedDateTime
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.time.Duration.Companion.milliseconds
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
    showAds = showAds,
    theme = theme,
    dynamicColorEnabled = dynamicColorEnabled,
    lastUpdate = lastUpdate.toZonedDateTime(),
    lastUpdateToAnalytics = lastUpdateToAnalytics?.toZonedDateTime(),
    shouldSync = true,
)

fun DomainPreferences.toFirestoreModel() = FirestoreUserPreferences(
    analyticsEnabled = analyticsEnabled,
    freeDriveAutoStart = freeDriveAutoStart,
    showAds = showAds,
    theme = theme,
    dynamicColorEnabled = dynamicColorEnabled,
    lastUpdate = lastUpdate.toTimestamp(),
    lastUpdateToAnalytics = lastUpdateToAnalytics?.toTimestamp(),
)

fun List<DomainLocation>.toPath(
    sessionUUID: String,
    ownerUUID: String
): FirestorePath {
    val pathLocations = map {
        FirestoreLocation(
            timestamp = it.zonedDateTime.toTimestamp(),
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

    val locationsBlob = Blob.fromBytes(Zstd.compress(ProtoBuf.encodeToByteArray(pathLocations)))
    val path = FirestorePath(
        uuid = UUID.nameUUIDFromBytes(locationsBlob.toBytes()).toString(),
        sessionUUID = sessionUUID,
        ownerUUID = ownerUUID,
        locations = locationsBlob
    )

    if (BuildConfig.DEBUG) {
        testCompressions(this)
    }

    return path
}

fun List<DomainSensorEvent>.toFirebaseSensorEvents(
    sessionUUID: String,
    ownerUUID: String
): FirestoreSensorEvents {
    val events = map {
        FirestoreSensorEvent(
            timestamp = it.zonedDateTime.toTimestamp(),
            accuracy = it.accuracy.toInt(),
            type = it.type.toInt(),
            x = it.x,
            y = it.y,
            z = it.z
        )
    }

    val eventsBlob = Blob.fromBytes(Zstd.compress(ProtoBuf.encodeToByteArray(events)))

    return FirestoreSensorEvents(
        uuid = UUID.nameUUIDFromBytes(eventsBlob.toBytes()).toString(),
        sessionUUID = sessionUUID,
        ownerUUID = ownerUUID,
        events = eventsBlob
    )
}

private fun testCompressions(domainLocations: List<DomainLocation>) {
    // Size comparison between raw location data path and compressed data structures.

    // Conclusion: Path reduces data size ~13% compared to raw location data with SessionUUID
    // and optimized sensory data (Short, Byte used instead of Int).
    // Raw, optimized location data without SessionUUID, is taking up ~49% less space
    // compared to raw location data with SessionUUID (repeated).

    // Size ratio in smaller data (60-100k bytes):
    // - List<DomainLocations> = 1.00
    // - FirestorePath = ~0.87
    // - List<LocationWithoutSessionIdOptimized> = ~0.51
    // - List<LocationWithoutSessionId> = ~0.51
    // - Base64 GZIP compressed List<LocationWithoutSessionIdOptimized> = ~0.16
    // - Base64 ZLIB compressed List<LocationWithoutSessionIdOptimized> = ~0.17
    // - Blob GZIP compressed = ~0.07
    // - Blob ZLIB compressed = ~0.07
    // - Blob Zstd compressed = ~0.09

    // GZIP and ZLIB start returning only their headers when data is below ~50k bytes.
    // This might be due to their window being too short, or something else.

    // Zstd compression performs better in bigger data sets (400-800k bytes),
    // providing a 10% reduction in size compared to GZIP and ZLIB.

    // Shorts/Bytes don't really make a difference,
    // unless the data structure's size is above 32 bits -> Use Int
    // Bigger data structures (Long/Double, 64 bits) cause a +7.5% size growth.
    // This growth then accumulates with more data,
    // so it's actually around 25% growth in large datasets.

    val optimizedLocations = domainLocations.map {
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

    val locations = domainLocations.map {
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

    val unoptimizedLocations = domainLocations.map {
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
        ::encodeWithGZIP to "GZIP",
        { array: ByteArray ->
            val zlibByteArrayOutputStream = ByteArrayOutputStream()
            val deflater = Deflater(Deflater.BEST_COMPRESSION)
            val deflaterOutputStream = DeflaterOutputStream(zlibByteArrayOutputStream, deflater)
            deflaterOutputStream.write(array)
            val compressedBytes = zlibByteArrayOutputStream.toByteArray()
            deflaterOutputStream.close()
            zlibByteArrayOutputStream.close()
            compressedBytes
        } to "ZLIB",
        { array: ByteArray ->
            Zstd.compress(array, Zstd.maxCompressionLevel())
        } to "Zstd"
    )

    val stringBuilder = StringBuilder()
    val startMilli = locations
        .minBy { it.zonedDateTime.toInstant().toEpochMilli() }
        .zonedDateTime.toInstant().toEpochMilli()
    val endMilli = locations
        .maxBy { it.zonedDateTime.toInstant().toEpochMilli() }
        .zonedDateTime.toInstant().toEpochMilli()
    val durationInMinutes = (endMilli - startMilli).milliseconds.inWholeMinutes
    stringBuilder.append("Compressing $durationInMinutes minutes of Location data\n")

    data.forEach {
        val byteArray = it.first
        stringBuilder.append("Data type: ${it.second}\n")

        compressions.forEach { algo ->
            val compressedBytes = algo.first(byteArray)
            val algoName = algo.second
            stringBuilder.append("$algoName: ${compressedBytes.size} bytes\n")
        }
    }
    Timber.d(stringBuilder.toString())
}

private fun encodeWithGZIP(byteArray: ByteArray): ByteArray {
    val gzipByteArrayOutputStream = ByteArrayOutputStream()
    val gzipOutputStream = GZIPOutputStream(gzipByteArrayOutputStream)
    gzipOutputStream.write(byteArray)
    val compressedBytes = gzipByteArrayOutputStream.toByteArray()
    gzipByteArrayOutputStream.close()
    return compressedBytes
}

private fun decodeWithGZIP(byteArray: ByteArray): ByteArray {
    val gzipByteArrayInputStream = ByteArrayInputStream(byteArray)
    val gzipInputStream = GZIPInputStream(gzipByteArrayInputStream)
    val decompressedBytes = gzipInputStream.readBytes()
    gzipInputStream.close()
    return decompressedBytes
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
    thresholdInMinutes: Int = 300
): List<FirestorePath> {
    if (isEmpty()) return emptyList()
    val startMilli = minOf { it.zonedDateTime.toInstant().toEpochMilli() }
    val groupedByTime = groupBy {
        (it.zonedDateTime.toInstant().toEpochMilli() - startMilli) /
                thresholdInMinutes.minutes.inWholeMilliseconds
    }
    return groupedByTime.map { groups ->
        groups.value
            .sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
            .toPath(sessionUUID, ownerUUID)
    }
}

fun List<DomainSensorEvent>.toChunkedFirebaseSensorEvents(
    sessionUUID: String,
    ownerUUID: String,
    thresholdInMinutes: Int = 30
): List<FirestoreSensorEvents> {
    if (isEmpty()) return emptyList()
    val startMilli = minOf { it.zonedDateTime.toInstant().toEpochMilli() }
    val groupedByTime = groupBy {
        (it.zonedDateTime.toInstant().toEpochMilli() - startMilli) /
                thresholdInMinutes.minutes.inWholeMilliseconds
    }
    return groupedByTime.map { groups ->
        groups.value
            .sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
            .toFirebaseSensorEvents(sessionUUID, ownerUUID)
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun List<FirestorePath>.toDomainLocations(): List<DomainLocation> {
    val domainLocations = mutableListOf<DomainLocation>()

    forEach { path ->
        val locations = ProtoBuf.decodeFromByteArray<List<FirestoreLocation>>(Zstd.decompress(path.locations.toBytes(), 1_000_000))
        domainLocations.addAll(locations.map { it.toDomainModel(path.sessionUUID) })
    }

    return domainLocations.sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
}

fun List<FirestoreSensorEvents>.toDomainSensorEvents(): List<DomainSensorEvent> {
    val domainSensorEvents = mutableListOf<DomainSensorEvent>()

    forEach { events ->
        val sensorEvents = ProtoBuf.decodeFromByteArray<List<FirestoreSensorEvent>>(Zstd.decompress(events.events.toBytes(), 1_000_000))
        domainSensorEvents.addAll(sensorEvents.map { it.toDomainModel(events.sessionUUID) })
    }

    return domainSensorEvents.sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
}

fun FirestoreLocation.toDomainModel(
    sessionUUID: String
) = DomainLocation(
    latitude = latitude,
    zonedDateTime = timestamp.toZonedDateTime(),
    longitude = longitude,
    speed = speed,
    sessionUUID = sessionUUID,
    accuracy = accuracy.toByte(),
    bearing = bearing.toShort(),
    bearingAccuracy = bearingAccuracy.toShort(),
    altitude = altitude.toShort(),
    speedAccuracy = speedAccuracy,
    verticalAccuracy = verticalAccuracy.toShort()
)

fun FirestoreSensorEvent.toDomainModel(
    sessionUUID: String
) = DomainSensorEvent(
    zonedDateTime = timestamp.toZonedDateTime(),
    sessionUUID = sessionUUID,
    accuracy = accuracy.toByte(),
    type = type.toByte(),
    x = x,
    y = y,
    z = z
)

fun DataStatus<FirestoreUser>.toDomainPreferencesStatus(): DataStatus<DomainPreferences> {
    return DataStatus(
        data = data?.run { preferences?.toDomainModel(uuid) },
        isLoading = isLoading
    )
}

fun DataStatus<FirestoreUser>.toDomainSessionsStatus(): DataStatus<List<DomainSession>> {
    return DataStatus(
        data = data?.run { sessions.map { it.toDomainModel(uuid) } },
        isLoading = isLoading
    )
}
