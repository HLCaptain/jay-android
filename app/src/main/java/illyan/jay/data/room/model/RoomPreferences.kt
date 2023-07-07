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

package illyan.jay.data.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import illyan.jay.domain.model.DomainPreferences
import illyan.jay.domain.model.Theme
import java.time.ZonedDateTime
import java.util.UUID

@Entity(
    tableName = "preferences",
)
data class RoomPreferences(
    @PrimaryKey
    val userUUID: String = UUID.randomUUID().toString(),
    val freeDriveAutoStart: Boolean = DomainPreferences.Default.freeDriveAutoStart,
    val analyticsEnabled: Boolean = DomainPreferences.Default.analyticsEnabled,
    val showAds: Boolean = DomainPreferences.Default.showAds,
    val theme: Theme = DomainPreferences.Default.theme,
    val dynamicColorEnabled: Boolean = DomainPreferences.Default.dynamicColorEnabled,
    val lastUpdate: Long = ZonedDateTime.now().toInstant().toEpochMilli(),
    val lastUpdateToAnalytics: Long? = null,
    val shouldSync: Boolean = DomainPreferences.Default.shouldSync,
)
