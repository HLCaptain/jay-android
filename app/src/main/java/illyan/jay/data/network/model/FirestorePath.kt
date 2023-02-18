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

package illyan.jay.data.network.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<GeoPoint, GeoPointParceler>
data class FirestorePath(
    var uuid: String = "",
    val sessionUUID: String = "", // reference of the session this path is part of
    val ownerUUID: String = "",
    val accuracyChangeTimestamps: List<Timestamp> = emptyList(),
    val accuracyChanges: List<Byte> = emptyList(),
    val altitudes: List<Short> = emptyList(),
    val bearingAccuracyChangeTimestamps: List<Timestamp> = emptyList(),
    val bearingAccuracyChanges: List<Short> = emptyList(),
    val bearings: List<Short> = emptyList(),
    val coords: List<GeoPoint> = emptyList(),
    val speeds: List<Float> = emptyList(),
    val speedAccuracyChangeTimestamps: List<Timestamp> = emptyList(),
    val speedAccuracyChanges: List<Float> = emptyList(),
    val timestamps: List<Timestamp> = emptyList(),
    val verticalAccuracyChangeTimestamps: List<Timestamp> = emptyList(),
    val verticalAccuracyChanges: List<Short> = emptyList()
) : Parcelable {
    companion object {
        const val CollectionName = "paths"
        const val FieldUUID = "uuid"
        const val FieldSessionUUID = "sessionUUID"
        const val FieldOwnerUUID = "ownerUUID"
        const val FieldAccuracyChangeTimestamps = "accuracyChangeTimestamps"
        const val FieldAccuracyChanges = "accuracyChanges"
        const val FieldAltitudes = "altitudes"
        const val FieldBearingAccuracyChangeTimestamps = "bearingAccuracyChangeTimestamps"
        const val FieldBearingAccuracyChanges = "bearingAccuracyChanges"
        const val FieldBearings = "bearings"
        const val FieldCoords = "coords"
        const val FieldSpeeds = "speeds"
        const val FieldSpeedAccuracyChangeTimestamps = "speedAccuracyChangeTimestamps"
        const val FieldSpeedAccuracyChanges = "speedAccuracyChanges"
        const val FieldTimestamps = "timestamps"
        const val FieldVerticalAccuracyChangeTimestamps = "verticalAccuracyChangeTimestamps"
        const val FieldVerticalAccuracyChanges = "verticalAccuracyChanges"
    }
}


