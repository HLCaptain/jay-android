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

package illyan.jay.ui.session.model

import com.google.android.gms.maps.model.LatLng
import illyan.jay.domain.model.DomainLocation
import java.time.ZonedDateTime

data class UiLocation(
    val zonedDateTime: ZonedDateTime,
    val latLng: LatLng,
    var speed: Float,
    var accuracy: Byte,
    var bearing: Short,
    var bearingAccuracy: Short, // in degrees
    var altitude: Short,
    var speedAccuracy: Float, // in meters per second
    var verticalAccuracy: Short, // in meters
    var aggression: Float? // Arbitrary unit, mostly in range [0.2, 1.0]
)

fun DomainLocation.toUiModel(aggression: Float? = null) = UiLocation(
    zonedDateTime = zonedDateTime,
    latLng = latLng,
    speed = speed,
    accuracy = accuracy,
    bearing = bearing,
    bearingAccuracy = bearingAccuracy,
    altitude = altitude,
    speedAccuracy = speedAccuracy,
    verticalAccuracy = verticalAccuracy,
    aggression = aggression
)
