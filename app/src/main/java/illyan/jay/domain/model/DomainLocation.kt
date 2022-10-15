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

package illyan.jay.domain.model

import com.google.android.gms.maps.model.LatLng
import java.time.ZonedDateTime

/**
 * Domain location used for general data handling
 * between DataSources, Interactors and Presenters.
 *
 * @property id
 * @property latLng
 * @property speed
 * @property sessionId
 * @property zonedDateTime
 * @property accuracy
 * @property bearing
 * @property bearingAccuracy
 * @property altitude
 * @property speedAccuracy
 * @property verticalAccuracy
 * @constructor Create empty Domain location
 */
data class DomainLocation(
    val id: Long = -1,
    val sessionId: Long,
    val zonedDateTime: ZonedDateTime,
    val latLng: LatLng,
    var speed: Float = -1f,
    var accuracy: Float = -1f,
    var bearing: Float = -1f,
    var bearingAccuracy: Float = -1f, // in degrees
    var altitude: Double = -1.0,
    var speedAccuracy: Float = -1f, // in meters per second
    var verticalAccuracy: Float = -1f // in meters
)
