package illyan.jay.data.disk

import android.hardware.SensorEvent
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import illyan.jay.data.disk.model.RoomAcceleration
import illyan.jay.data.disk.model.RoomLocation
import illyan.jay.data.disk.model.RoomRotation
import illyan.jay.data.disk.model.RoomSession
import illyan.jay.domain.model.DomainAcceleration
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainRotation
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.sensorTimestampToAbsoluteTime
import java.time.Instant
import java.util.*

// Session
fun RoomSession.toDomainModel() = DomainSession(
    id = id,
    startTime = Date.from(Instant.ofEpochMilli(startTime)),
    endTime = endTime?.let { Date.from(Instant.ofEpochMilli(it)) },
    distance = distance
)

fun DomainSession.toRoomModel() = RoomSession(
    id = id,
    startTime = startTime.time,
    endTime = endTime?.time,
    distance = distance
)

// Location
fun RoomLocation.toDomainModel() = DomainLocation(
    id = id,
    latLng = LatLng(latitude, longitude),
    speed = speed,
    sessionId = sessionId,
    time = Date.from(Instant.ofEpochMilli(time)),
    accuracy = accuracy,
    bearing = bearing,
    bearingAccuracy = bearingAccuracy,
    altitude = altitude,
    speedAccuracy = speedAccuracy,
    verticalAccuracy = verticalAccuracy
)

fun DomainLocation.toRoomModel() = RoomLocation(
    latitude = latLng.latitude,
    longitude = latLng.longitude,
    speed = speed,
    sessionId = sessionId,
    time = time.time,
    accuracy = accuracy,
    bearing = bearing,
    bearingAccuracy = bearingAccuracy,
    altitude = altitude,
    speedAccuracy = speedAccuracy,
    verticalAccuracy = verticalAccuracy
)

fun Location.toDomainModel(sessionId: Int) = DomainLocation(
    latLng = LatLng(latitude, longitude),
    speed = speed,
    time = Date.from(Instant.ofEpochMilli(time)),
    sessionId = sessionId,
    accuracy = accuracy,
    bearing = bearing,
    bearingAccuracy = bearingAccuracyDegrees,
    altitude = altitude,
    speedAccuracy = speedAccuracyMetersPerSecond,
    verticalAccuracy = verticalAccuracyMeters
)

// Rotation
fun RoomRotation.toDomainModel() = DomainRotation(
    id = id,
    time = Date.from(Instant.ofEpochMilli(time)),
    sessionId = sessionId,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z
)

fun DomainRotation.toRoomModel() = RoomRotation(
    time = time.time,
    sessionId = sessionId,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z
)

// Acceleration
fun RoomAcceleration.toDomainModel() = DomainAcceleration(
    id = id,
    time = Date.from(Instant.ofEpochMilli(time)),
    sessionId = sessionId,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z
)

fun DomainAcceleration.toRoomModel() = RoomAcceleration(
    id = id,
    time = time.time,
    sessionId = sessionId,
    accuracy = accuracy,
    x = x,
    y = y,
    z = z
)

// Sensors
fun SensorEvent.toDomainRotation(sessionId: Int) = DomainRotation(
    sessionId = sessionId,
    time = Date.from(Instant.ofEpochMilli(sensorTimestampToAbsoluteTime(timestamp))),
    accuracy = accuracy,
    x = values[0],
    y = values[1],
    z = values[2]
)

fun SensorEvent.toDomainAcceleration(sessionId: Int) = DomainAcceleration(
    sessionId = sessionId,
    time = Date.from(Instant.ofEpochMilli(sensorTimestampToAbsoluteTime(timestamp))),
    accuracy = accuracy,
    x = values[0],
    y = values[1],
    z = values[2]
)