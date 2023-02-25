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

import illyan.jay.data.disk.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime


@Serializable
data class DomainPreferences(
    val userUUID: String? = null,
    val analyticsEnabled: Boolean = false,
    val freeDriveAutoStart: Boolean = false,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val lastUpdate: ZonedDateTime = ZonedDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        return if (other != null && other is DomainPreferences) {
            lastUpdate.toEpochSecond() == other.lastUpdate.toEpochSecond() &&
                    userUUID == other.userUUID &&
                    freeDriveAutoStart == other.freeDriveAutoStart
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = userUUID?.hashCode() ?: 0
        result = 31 * result + analyticsEnabled.hashCode()
        result = 31 * result + freeDriveAutoStart.hashCode()
        result = 31 * result + lastUpdate.toEpochSecond().hashCode()
        return result
    }

    companion object {
        val default = DomainPreferences()
    }
}