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
 * between DataSources, Interactors and ViewModels.
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
    val sessionId: Int,
    val zonedDateTime: ZonedDateTime,
    val latitude: Float,
    val longitude: Float,
    var speed: Float = Float.NaN,
    var accuracy: Byte = Byte.MIN_VALUE,
    var bearing: Short = Short.MIN_VALUE,
    var bearingAccuracy: Short = Short.MIN_VALUE, // in degrees
    var altitude: Short = Short.MIN_VALUE,
    var speedAccuracy: Byte = Byte.MIN_VALUE, // in meters per second
    var verticalAccuracy: Short = Short.MIN_VALUE // in meters
) {
    val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
}
