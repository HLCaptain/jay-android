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

package illyan.jay.ui.settings.user.model

import illyan.jay.domain.model.DomainPreferences
import java.time.ZonedDateTime

data class UiPreferences(
    val userUUID: String? = null,
    val analyticsEnabled: Boolean = DomainPreferences.Default.analyticsEnabled,
    val freeDriveAutoStart: Boolean = DomainPreferences.Default.freeDriveAutoStart,
    val showAds: Boolean = DomainPreferences.Default.showAds,
    val lastUpdate: ZonedDateTime = DomainPreferences.Default.lastUpdate,
    val lastUpdateToAnalytics: ZonedDateTime? = null,
    val clientUUID: String? = null,
)

fun DomainPreferences.toUiModel(
    clientUUID: String? = null
) = UiPreferences(
    userUUID = userUUID,
    analyticsEnabled = analyticsEnabled,
    freeDriveAutoStart = freeDriveAutoStart,
    showAds = showAds,
    lastUpdate = lastUpdate,
    lastUpdateToAnalytics = lastUpdateToAnalytics,
    clientUUID = clientUUID,
)
