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

@file:OptIn(ExperimentalTime::class)

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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Session
fun RoomSession.toDomainModel() = DomainSession(
    uuid = uuid,
    startDateTime = Instant.fromEpochMilliseconds(startDateTime),
    endDateTime = endDateTime?.let { Instant.fromEpochMilliseconds(it) },
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

@OptIn(ExperimentalTime::class)
fun DomainSession.toRoomModel() = RoomSession(
    uuid = uuid,
    startDateTime = startDateTime.toEpochMilliseconds(),
    endDateTime = endDateTime?.toEpochMilliseconds(),
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
    timestamp = Instant.fromEpochMilliseconds(time),
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
    time = timestamp.toEpochMilliseconds(),
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
        timestamp = Instant.fromEpochMilliseconds(time),
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
    timestamp = Instant.fromEpochMilliseconds(time),
    sessionUUID = sessionUUID,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z,
    type = type
)

fun DomainSensorEvent.toRoomModel() = RoomSensorEvent(
    sessionUUID = sessionUUID,
    time = timestamp.toEpochMilliseconds(),
    type = type,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z
)

// Sensors
fun SensorEvent.toDomainModel(sessionUUID: String) = DomainSensorEvent(
    sessionUUID = sessionUUID,
    timestamp = Instant.fromEpochMilliseconds(sensorTimestampToAbsoluteTime(timestamp)),
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
    lastUpdate = Instant.fromEpochMilliseconds(lastUpdate),
    lastUpdateToAnalytics = lastUpdateToAnalytics?.let { return@let Instant.fromEpochMilliseconds(it) },
    shouldSync = shouldSync,
)

fun DomainPreferences.toRoomModel(
    userUUID: String,
    lastUpdate: Instant = this.lastUpdate
) = RoomPreferences(
    userUUID = userUUID,
    analyticsEnabled = analyticsEnabled,
    freeDriveAutoStart = freeDriveAutoStart,
    showAds = showAds,
    theme = theme,
    dynamicColorEnabled = dynamicColorEnabled,
    lastUpdate = lastUpdate.toEpochMilliseconds(),
    lastUpdateToAnalytics = lastUpdateToAnalytics?.toEpochMilliseconds(),
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
