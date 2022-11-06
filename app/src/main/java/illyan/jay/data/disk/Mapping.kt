/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.disk

import android.hardware.SensorEvent
import android.location.Location
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import illyan.jay.data.disk.model.RoomLocation
import illyan.jay.data.disk.model.RoomSensorEvent
import illyan.jay.data.disk.model.RoomSession
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSensorEvent
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.sensorTimestampToAbsoluteTime
import java.time.Instant
import java.time.ZoneOffset

// Session
fun RoomSession.toDomainModel() = DomainSession(
    id = id,
    uuid = uuid,
    startDateTime = Instant.ofEpochMilli(startDateTime).atZone(ZoneOffset.UTC),
    endDateTime = endDateTime?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC) },
    startLocationLatitude = startLocationLatitude,
    startLocationLongitude = startLocationLongitude,
    endLocationLatitude = endLocationLatitude,
    endLocationLongitude = endLocationLongitude,
    startLocationName = startLocationName,
    endLocationName = endLocationName,
)

fun DomainSession.toRoomModel() = RoomSession(
    id = id,
    uuid = uuid,
    startDateTime = startDateTime.toInstant().toEpochMilli(),
    endDateTime = endDateTime?.toInstant()?.toEpochMilli(),
    startLocationLatitude = startLocationLatitude,
    startLocationLongitude = startLocationLongitude,
    endLocationLatitude = endLocationLatitude,
    endLocationLongitude = endLocationLongitude,
    startLocationName = startLocationName,
    endLocationName = endLocationName,
)

// Location
fun RoomLocation.toDomainModel() = DomainLocation(
    id = id,
    latitude = latitude,
    longitude = longitude,
    speed = speed,
    sessionId = sessionId,
    zonedDateTime = Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC),
    accuracy = accuracy,
    bearing = bearing,
    bearingAccuracy = bearingAccuracy,
    altitude = altitude,
    speedAccuracy = speedAccuracy,
    verticalAccuracy = verticalAccuracy
)

fun DomainLocation.toRoomModel() = RoomLocation(
    latitude = latitude,
    longitude = longitude,
    speed = speed,
    sessionId = sessionId,
    time = zonedDateTime.toInstant().toEpochMilli(),
    accuracy = accuracy,
    bearing = bearing,
    bearingAccuracy = bearingAccuracy,
    altitude = altitude,
    speedAccuracy = speedAccuracy,
    verticalAccuracy = verticalAccuracy
)

fun Location.toDomainModel(sessionId: Long): DomainLocation {
    val domainLocation = DomainLocation(
        latitude = latitude.toFloat(),
        longitude = longitude.toFloat(),
        sessionId = sessionId.toInt(),
        zonedDateTime = Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC)
    )

    if (hasSpeed()) domainLocation.speed = speed
    if (hasAccuracy()) domainLocation.accuracy = accuracy.toInt().toByte()
    if (hasBearing()) domainLocation.bearing = bearing.toInt().toShort()
    if (hasAltitude()) domainLocation.altitude = altitude.toInt().toShort()

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
        if (hasBearingAccuracy()) domainLocation.bearingAccuracy = bearingAccuracyDegrees.toInt().toShort()
        if (hasSpeedAccuracy()) domainLocation.speedAccuracy = speedAccuracyMetersPerSecond.toInt().toByte()
        if (hasVerticalAccuracy()) domainLocation.verticalAccuracy = verticalAccuracyMeters.toInt().toShort()
    }
    return domainLocation
}

// Rotation
fun RoomSensorEvent.toDomainModel() = DomainSensorEvent(
    id = id,
    zonedDateTime = Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC),
    sessionId = sessionId,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z,
    type = type
)

fun DomainSensorEvent.toRoomModel() = RoomSensorEvent(
    time = zonedDateTime.toInstant().toEpochMilli(),
    sessionId = sessionId,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z,
    type = type
)

// Sensors
fun SensorEvent.toDomainModel(sessionId: Long) = DomainSensorEvent(
    sessionId = sessionId.toInt(),
    zonedDateTime = Instant.ofEpochMilli(sensorTimestampToAbsoluteTime(timestamp)).atZone(ZoneOffset.UTC),
    accuracy = accuracy.toByte(),
    x = values[0],
    y = values[1],
    z = values[2],
    type = sensor.type.toByte()
)
