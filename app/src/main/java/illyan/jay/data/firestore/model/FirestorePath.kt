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

package illyan.jay.data.firestore.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirestorePath(
    @DocumentId
    val uuid: String = "",
    @PropertyName(FieldSessionUUID) val sessionUUID: String = "", // reference of the session this path is part of
    @PropertyName(FieldOwnerUUID) val ownerUUID: String = "",
    @PropertyName(FieldLocations) val locations: Blob = Blob.fromBytes(ByteArray(0)),
    @PropertyName(FieldAggressions) val aggressions: Blob? = null
) {
    companion object {
        const val CollectionName = "paths"
        const val FieldSessionUUID = "sessionUUID"
        const val FieldOwnerUUID = "ownerUUID"
        const val FieldLocations = "locations"
        const val FieldAggressions = "aggressions"
    }
}


