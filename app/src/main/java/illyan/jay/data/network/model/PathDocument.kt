/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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
import java.util.UUID

data class PathDocument(
    var uuid: String = UUID.randomUUID().toString(),
    val accuracyChangeTimestamps: List<Timestamp>,
    val accuracyChanges: List<Byte>,
    val altitudes: List<Short>,
    val bearingAccuracyChangeTimestamps: List<Timestamp>,
    val bearingAccuracyChanges: List<Short>,
    val bearings: List<Short>,
    val coords: List<GeoPoint>,
    val sessionUUID: String, // reference of the session this path is part of
    val speeds: List<Float>,
    val speedAccuracyChangeTimestamps: List<Timestamp>,
    val speedAccuracyChanges: List<Float>,
    val timestamps: List<Timestamp>,
    val verticalAccuracyChangeTimestamps: List<Timestamp>,
    val verticalAccuracyChanges: List<Short>
)
