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

package illyan.jay.data.firestore.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName
import illyan.jay.util.toTimestamp
import java.time.Instant

data class FirestoreSession(
    @PropertyName(FieldUUID) val uuid: String = "",
    @PropertyName(FieldStartDateTime) val startDateTime: Timestamp = Instant.EPOCH.toTimestamp(),
    @PropertyName(FieldEndDateTime) val endDateTime: Timestamp? = null,
    @PropertyName(FieldStartLocation) val startLocation: GeoPoint? = null,
    @PropertyName(FieldEndLocation) val endLocation: GeoPoint? = null,
    @PropertyName(FieldStartLocationName) val startLocationName: String? = null,
    @PropertyName(FieldEndLocationName) val endLocationName: String? = null,
    @PropertyName(FieldDistance) val distance: Float? = null,
    @PropertyName(FieldClientUUID) val clientUUID: String? = null
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