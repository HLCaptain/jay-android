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

package illyan.jay.data.disk.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location",
    foreignKeys = [
        ForeignKey(
            entity = RoomSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"]
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class RoomLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val time: Long, // in millis
    val speed: Float,
    val speedAccuracy: Float, // in meters per second
    val bearing: Float,
    val bearingAccuracy: Float, // in degrees
    val altitude: Double,
    val verticalAccuracy: Float // in meters
)
