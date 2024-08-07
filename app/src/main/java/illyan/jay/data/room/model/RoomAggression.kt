/*
 * Copyright (c) 2024 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "aggressions",
    foreignKeys = [
        ForeignKey(
            entity = RoomSession::class,
            parentColumns = ["uuid"],
            childColumns = ["sessionUUID"]
        )
    ],
    indices = [Index(value = ["sessionUUID"])],
    primaryKeys = ["sessionUUID", "timestamp"]
)
data class RoomAggression(
    val sessionUUID: String,
    val timestamp: Long, // in millis
    val aggression: Float
)
