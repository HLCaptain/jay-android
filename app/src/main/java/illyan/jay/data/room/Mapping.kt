/*
 * Copyright (c) 2022-2024 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.room

import android.hardware.SensorEvent
import android.location.Location
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import illyan.jay.data.room.model.RoomAggression
import illyan.jay.data.room.model.RoomLocation
import illyan.jay.data.room.model.RoomPreferences
import illyan.jay.data.room.model.RoomSensorEvent
import illyan.jay.data.room.model.RoomSession
import illyan.jay.domain.model.DomainAggression
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainPreferences
import illyan.jay.domain.model.DomainSensorEvent
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.sensorTimestampToAbsoluteTime
import illyan.jay.util.toZonedDateTime
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

// Session
fun RoomSession.toDomainModel() = DomainSession(
    uuid = uuid,
    startDateTime = Instant.ofEpochMilli(startDateTime).atZone(ZoneOffset.UTC),
    endDateTime = endDateTime?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC) },
    startLocationLatitude = startLocationLatitude,
    startLocationLongitude = startLocationLongitude,
    endLocationLatitude = endLocationLatitude,
    endLocationLongitude = endLocationLongitude,
    startLocationName = startLocationName,
    endLocationName = endLocationName,
    distance = distance,
    ownerUUID = ownerUUID,
    clientUUID = clientUUID,
)

fun DomainSession.toRoomModel() = RoomSession(
    uuid = uuid,
    startDateTime = startDateTime.toInstant().toEpochMilli(),
    endDateTime = endDateTime?.toInstant()?.toEpochMilli(),
    startLocationLatitude = startLocationLatitude,
    startLocationLongitude = startLocationLongitude,
    endLocationLatitude = endLocationLatitude,
    endLocationLongitude = endLocationLongitude,
    startLocationName = startLocationName,
    endLocationName = endLocationName,
    distance = distance,
    ownerUUID = ownerUUID,
    clientUUID = clientUUID,
)

// Location
fun RoomLocation.toDomainModel() = DomainLocation(
    latitude = latitude,
    zonedDateTime = Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC),
    longitude = longitude,
    speed = speed,
    sessionUUID = sessionUUID,
    accuracy = accuracy,
    bearing = bearing,
    bearingAccuracy = bearingAccuracy,
    altitude = altitude,
    speedAccuracy = speedAccuracy,
    verticalAccuracy = verticalAccuracy
)

fun DomainLocation.toRoomModel() = RoomLocation(
    sessionUUID = sessionUUID,
    time = zonedDateTime.toInstant().toEpochMilli(),
    latitude = latitude,
    longitude = longitude,
    speed = speed,
    accuracy = accuracy,
    bearing = bearing,
    bearingAccuracy = bearingAccuracy,
    altitude = altitude,
    speedAccuracy = speedAccuracy,
    verticalAccuracy = verticalAccuracy
)

fun Location.toDomainModel(
    sessionUUID: String
): DomainLocation {
    val domainLocation = DomainLocation(
        sessionUUID = sessionUUID,
        zonedDateTime = Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC),
        latitude = latitude.toFloat(),
        longitude = longitude.toFloat()
    )

    if (hasSpeed()) domainLocation.speed = speed
    if (hasAccuracy()) domainLocation.accuracy = accuracy.toInt().toByte()
    if (hasBearing()) domainLocation.bearing = bearing.toInt().toShort()
    if (hasAltitude()) domainLocation.altitude = altitude.toInt().toShort()

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
        if (hasBearingAccuracy()) domainLocation.bearingAccuracy = bearingAccuracyDegrees.toInt().toShort()
        if (hasSpeedAccuracy()) domainLocation.speedAccuracy = speedAccuracyMetersPerSecond
        if (hasVerticalAccuracy()) domainLocation.verticalAccuracy = verticalAccuracyMeters.toInt().toShort()
    }
    return domainLocation
}

// Rotation
fun RoomSensorEvent.toDomainModel() = DomainSensorEvent(
    zonedDateTime = Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC),
    sessionUUID = sessionUUID,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z,
    type = type
)

fun DomainSensorEvent.toRoomModel() = RoomSensorEvent(
    sessionUUID = sessionUUID,
    time = zonedDateTime.toInstant().toEpochMilli(),
    type = type,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z
)

// Sensors
fun SensorEvent.toDomainModel(sessionUUID: String) = DomainSensorEvent(
    sessionUUID = sessionUUID,
    zonedDateTime = Instant.ofEpochMilli(sensorTimestampToAbsoluteTime(timestamp)).atZone(ZoneOffset.UTC),
    type = sensor.type.toByte(),
    accuracy = accuracy.toByte(),
    x = values[0],
    y = values[1],
    z = values[2]
)

// Preferences
fun RoomPreferences.toDomainModel() = DomainPreferences(
    userUUID = userUUID,
    analyticsEnabled = analyticsEnabled,
    freeDriveAutoStart = freeDriveAutoStart,
    showAds = showAds,
    theme = theme,
    dynamicColorEnabled = dynamicColorEnabled,
    lastUpdate = Instant.ofEpochMilli(lastUpdate).toZonedDateTime(),
    lastUpdateToAnalytics = lastUpdateToAnalytics?.let { return@let Instant.ofEpochMilli(it).toZonedDateTime() },
    shouldSync = shouldSync,
)

fun DomainPreferences.toRoomModel(
    userUUID: String,
    lastUpdate: ZonedDateTime = this.lastUpdate
) = RoomPreferences(
    userUUID = userUUID,
    analyticsEnabled = analyticsEnabled,
    freeDriveAutoStart = freeDriveAutoStart,
    showAds = showAds,
    theme = theme,
    dynamicColorEnabled = dynamicColorEnabled,
    lastUpdate = lastUpdate.toInstant().toEpochMilli(),
    lastUpdateToAnalytics = lastUpdateToAnalytics?.toInstant()?.toEpochMilli(),
    shouldSync = shouldSync,
)

fun RoomAggression.toDomainModel() = DomainAggression(
    sessionUUID = sessionUUID,
    timestamp = timestamp,
    aggression = aggression
)

fun DomainAggression.toRoomModel() = RoomAggression(
    sessionUUID = sessionUUID,
    timestamp = timestamp,
    aggression = aggression
)
