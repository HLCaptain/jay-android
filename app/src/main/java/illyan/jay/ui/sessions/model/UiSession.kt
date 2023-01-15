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

package illyan.jay.ui.sessions.model

import com.google.android.gms.maps.model.LatLng
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.sphericalPathLength
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class UiSession(
    val uuid: String,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime?,
    val startCoordinate: LatLng?,
    val endCoordinate: LatLng?,
    val totalDistance: Double,
    val startLocationName: String?,
    val endLocationName: String?,
    val duration: Duration,
    val isLocal: Boolean,
    val isSynced: Boolean,
    val clientUUID: String?
) {
    val isOwned get() = !clientUUID.isNullOrBlank()
    val isNotOwned get() = !isOwned
}

fun DomainSession.toUiModel(
    locations: List<DomainLocation>,
    currentClientUUID: String,
    isLocal: Boolean = clientUUID == currentClientUUID,
    currentTime: ZonedDateTime = ZonedDateTime.now(),
    isSynced: Boolean = false,
): UiSession {
    return toUiModel(
        locations.sphericalPathLength(),
        currentClientUUID,
        isLocal,
        currentTime,
        isSynced
    )
}

fun DomainSession.toUiModel(
    totalDistance: Double = distance?.toDouble() ?: -1.0,
    currentClientUUID: String,
    isLocal: Boolean = clientUUID == currentClientUUID,
    currentTime: ZonedDateTime = ZonedDateTime.now(),
    isSynced: Boolean = false,
): UiSession {
    return UiSession(
        uuid = uuid,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        startCoordinate = startLocation,
        endCoordinate = endLocation,
        totalDistance = totalDistance,
        startLocationName = startLocationName,
        endLocationName = endLocationName,
        duration = if (endDateTime != null) {
            (endDateTime!!.toInstant().toEpochMilli() - startDateTime.toInstant().toEpochMilli())
                .milliseconds
        } else {
            (currentTime.toInstant().toEpochMilli() - startDateTime.toInstant().toEpochMilli())
                .milliseconds
        },
        isSynced = isSynced,
        isLocal = isLocal,
        clientUUID = clientUUID
    )
}