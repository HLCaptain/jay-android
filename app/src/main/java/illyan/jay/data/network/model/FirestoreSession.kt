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

data class FirestoreSession(
    val uuid: String,
    val startDateTime: Timestamp,
    val endDateTime: Timestamp?,
    val startLocation: GeoPoint? = null,
    val endLocation: GeoPoint? = null,
    val startLocationName: String? = null,
    val endLocationName: String? = null,
    val distance: Float? = null,
    val clientUUID: String? = null
) {
    companion object {
        const val FieldUUID = "uuid"
    }
}