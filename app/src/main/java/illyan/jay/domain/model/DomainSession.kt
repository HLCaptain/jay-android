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
 * Domain session used for general data handling
 * between DataSources, Interactors and Presenters.
 *
 * @property id
 * @property startDateTime
 * @property endDateTime
 * @constructor Create empty Domain session
 */
data class DomainSession(
    val id: Long = -1,
    val startDateTime: ZonedDateTime,
    var endDateTime: ZonedDateTime?,
    var startLocationLatitude: Float? = null,
    var startLocationLongitude: Float? = null,
    var endLocationLatitude: Float? = null,
    var endLocationLongitude: Float? = null,
    var startLocationName: String? = null,
    var endLocationName: String? = null
) {
    var startLocation: LatLng?
        get() {
            return if (startLocationLatitude != null && startLocationLongitude != null) {
                LatLng(
                    startLocationLatitude!!.toDouble(),
                    startLocationLongitude!!.toDouble()
                )
            } else {
                null
            }
        }
        set(value) {
            if (startLocationLatitude == null || startLocationLongitude == null) {
                startLocationLatitude = value?.latitude?.toFloat()
                startLocationLongitude = value?.longitude?.toFloat()
            }
        }
    var endLocation: LatLng?
        get() {
            return if (endLocationLatitude != null && endLocationLongitude != null) {
                LatLng(
                    endLocationLatitude!!.toDouble(),
                    endLocationLongitude!!.toDouble()
                )
            } else {
                null
            }
        }
        set(value) {
            if (endLocationLatitude == null || endLocationLongitude == null) {
                endLocationLatitude = value?.latitude?.toFloat()
                endLocationLongitude = value?.longitude?.toFloat()
            }
        }
}
