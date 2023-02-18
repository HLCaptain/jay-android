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
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<GeoPoint, GeoPointParceler>
data class FirestorePath(
    @get:PropertyName(FieldUUID)
    @set:PropertyName(FieldUUID)
    var uuid: String = "",

    @get:PropertyName(FieldSessionUUID)
    @set:PropertyName(FieldSessionUUID)
    var sessionUUID: String = "", // reference of the session this path is part of

    @get:PropertyName(FieldOwnerUUID)
    @set:PropertyName(FieldOwnerUUID)
    var ownerUUID: String = "",

    @get:PropertyName(FieldAccuracyChangeTimestamps)
    @set:PropertyName(FieldAccuracyChangeTimestamps)
    var accuracyChangeTimestamps: List<Timestamp> = emptyList(),

    @get:PropertyName(FieldAccuracyChanges)
    @set:PropertyName(FieldAccuracyChanges)
    var accuracyChanges: List<Byte> = emptyList(),

    @get:PropertyName(FieldAltitudes)
    @set:PropertyName(FieldAltitudes)
    var altitudes: List<Short> = emptyList(),

    @get:PropertyName(FieldBearingAccuracyChangeTimestamps)
    @set:PropertyName(FieldBearingAccuracyChangeTimestamps)
    var bearingAccuracyChangeTimestamps: List<Timestamp> = emptyList(),

    @get:PropertyName(FieldBearingAccuracyChanges)
    @set:PropertyName(FieldBearingAccuracyChanges)
    var bearingAccuracyChanges: List<Short> = emptyList(),

    @get:PropertyName(FieldBearings)
    @set:PropertyName(FieldBearings)
    var bearings: List<Short> = emptyList(),

    @get:PropertyName(FieldCoords)
    @set:PropertyName(FieldCoords)
    var coords: List<GeoPoint> = emptyList(),

    @get:PropertyName(FieldSpeeds)
    @set:PropertyName(FieldSpeeds)
    var speeds: List<Float> = emptyList(),

    @get:PropertyName(FieldSpeedAccuracyChangeTimestamps)
    @set:PropertyName(FieldSpeedAccuracyChangeTimestamps)
    var speedAccuracyChangeTimestamps: List<Timestamp> = emptyList(),

    @get:PropertyName(FieldSpeedAccuracyChanges)
    @set:PropertyName(FieldSpeedAccuracyChanges)
    var speedAccuracyChanges: List<Float> = emptyList(),

    @get:PropertyName(FieldTimestamps)
    @set:PropertyName(FieldTimestamps)
    var timestamps: List<Timestamp> = emptyList(),

    @get:PropertyName(FieldVerticalAccuracyChangeTimestamps)
    @set:PropertyName(FieldVerticalAccuracyChangeTimestamps)
    var verticalAccuracyChangeTimestamps: List<Timestamp> = emptyList(),

    @get:PropertyName(FieldVerticalAccuracyChanges)
    @set:PropertyName(FieldVerticalAccuracyChanges)
    var verticalAccuracyChanges: List<Short> = emptyList()
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


