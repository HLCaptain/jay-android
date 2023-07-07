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

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import illyan.jay.domain.model.DomainPreferences
import illyan.jay.domain.model.Theme

data class FirestoreUserPreferences(
    @PropertyName(FieldAnalyticsEnabled) val analyticsEnabled: Boolean = DomainPreferences.Default.analyticsEnabled,
    @PropertyName(FieldFreeDriveAutoStart) val freeDriveAutoStart: Boolean = DomainPreferences.Default.freeDriveAutoStart,
    @PropertyName(FieldShowAds) val showAds: Boolean = DomainPreferences.Default.showAds,
    @PropertyName(FieldTheme) val theme: Theme = DomainPreferences.Default.theme,
    @PropertyName(FieldDynamicColorEnabled) val dynamicColorEnabled: Boolean = DomainPreferences.Default.dynamicColorEnabled,
    @PropertyName(FieldLastUpdate) val lastUpdate: Timestamp = Timestamp.now(),
    @PropertyName(FieldLastUpdateToAnalytics) val lastUpdateToAnalytics: Timestamp? = null,
) {
    companion object {
        const val FieldAnalyticsEnabled = "analyticsEnabled"
        const val FieldFreeDriveAutoStart = "freeDriveAutoStart"
        const val FieldLastUpdate = "lastUpdate"
        const val FieldTheme = "theme"
        const val FieldDynamicColorEnabled = "dynamicColorEnabled"
        const val FieldShowAds = "showAds"
        const val FieldLastUpdateToAnalytics = "lastUpdateToAnalytics"
    }
}
