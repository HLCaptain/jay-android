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

package illyan.jay.domain.model

import illyan.jay.data.serializers.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
@Serializable
data class DomainPreferences @OptIn(ExperimentalTime::class) constructor(
    val userUUID: String? = null,
    val analyticsEnabled: Boolean = false,
    val freeDriveAutoStart: Boolean = false,
    val showAds: Boolean = false,
    val theme: Theme = Theme.System,
    val dynamicColorEnabled: Boolean = true,
    @Serializable(with = InstantSerializer::class)
    val lastUpdate: Instant = Clock.System.now(),
    @Serializable(with = InstantSerializer::class)
    val lastUpdateToAnalytics: Instant? = null,
    val shouldSync: Boolean = false,
) {
    fun isBefore(other: DomainPreferences): Boolean {
        return lastUpdate < other.lastUpdate
    }

    fun isAfter(other: DomainPreferences): Boolean {
        return lastUpdate > other.lastUpdate
    }

    companion object {
        val Default = DomainPreferences()
    }
}
