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

import android.os.Parcelable
import com.google.firebase.Timestamp
import illyan.jay.data.firestore.serializers.TimestampSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class FirestoreSensorEvent(
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Timestamp,
    val type: Int,
    val accuracy: Int, // enum
    val x: Float,
    val y: Float,
    val z: Float,
) : Parcelable
