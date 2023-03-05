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

package illyan.jay.data.network.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import illyan.jay.domain.model.DomainPreferences

data class FirestoreUserPreferences(
    @PropertyName(FieldAnalyticsEnabled) val analyticsEnabled: Boolean = DomainPreferences.default.analyticsEnabled,
    @PropertyName(FieldFreeDriveAutoStart) val freeDriveAutoStart: Boolean = DomainPreferences.default.freeDriveAutoStart,
    @PropertyName(FieldShowAds) val showAds: Boolean = DomainPreferences.default.showAds,
    @PropertyName(FieldLastUpdate) val lastUpdate: Timestamp = Timestamp.now(),
) {
    companion object {
        const val FieldAnalyticsEnabled = "analyticsEnabled"
        const val FieldFreeDriveAutoStart = "freeDriveAutoStart"
        const val FieldLastUpdate = "lastUpdate"
        const val FieldShowAds = "showAds"
    }
}
