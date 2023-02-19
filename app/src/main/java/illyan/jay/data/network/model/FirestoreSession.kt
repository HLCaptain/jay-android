/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.network.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import illyan.jay.util.toTimestamp
import java.time.Instant

data class FirestoreSession(
    var uuid: String = "",
    var startDateTime: Timestamp = Instant.EPOCH.toTimestamp(),
    var endDateTime: Timestamp? = null,
    var startLocation: GeoPoint? = null,
    var endLocation: GeoPoint? = null,
    var startLocationName: String? = null,
    var endLocationName: String? = null,
    var distance: Float? = null,
    var clientUUID: String? = null
) {
    companion object {
        const val FieldUUID = "uuid"
        const val FieldStartDateTime = "startDateTime"
        const val FieldEndDateTime = "endDateTime"
        const val FieldStartLocation = "startLocation"
        const val FieldEndLocation = "endLocation"
        const val FieldStartLocationName = "startLocationName"
        const val FieldEndLocationName = "endLocationName"
        const val FieldDistance = "distance"
        const val FieldClientUUID = "clientUUID"
    }
}