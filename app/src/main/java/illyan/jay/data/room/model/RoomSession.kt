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

package illyan.jay.data.room.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sessions",
    indices = [Index(value = ["uuid"])]
)
data class RoomSession(
    @PrimaryKey
    val uuid: String = UUID.randomUUID().toString(),
    val startDateTime: Long,
    var endDateTime: Long? = null,
    var startLocationLatitude: Float? = null,
    var startLocationLongitude: Float? = null,
    var endLocationLatitude: Float? = null,
    var endLocationLongitude: Float? = null,
    var startLocationName: String? = null,
    var endLocationName: String? = null,
    val distance: Float? = null,
    val ownerUUID: String? = null,
    val clientUUID: String? = null,
)
