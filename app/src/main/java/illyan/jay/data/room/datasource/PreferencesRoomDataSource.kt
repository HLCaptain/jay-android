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

package illyan.jay.data.room.datasource

import illyan.jay.data.room.dao.PreferencesDao
import illyan.jay.data.room.toDomainModel
import illyan.jay.data.room.toRoomModel
import illyan.jay.domain.model.DomainPreferences
import illyan.jay.domain.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class PreferencesRoomDataSource @Inject constructor(
    private val preferencesDao: PreferencesDao
) {
    fun getPreferences(userUUID: String): Flow<DomainPreferences?> {
        Timber.v("Getting preferences flow for user ${userUUID.take(4)}")
        return preferencesDao.getPreferences(userUUID).map {
            it?.toDomainModel()
        }
    }

    fun upsertPreferences(domainPreferences: DomainPreferences) {
        if (domainPreferences.userUUID != null) {
            Timber.v("Upserting $domainPreferences")
            preferencesDao.upsertPreferences(
                domainPreferences.toRoomModel(userUUID = domainPreferences.userUUID)
            )
        } else {
            Timber.e(
                IllegalArgumentException(
                    "Cannot save preferences with no user associated with it via Room!\n" +
                            "Offline user's preferences is saved per-app in [AppSettings]."
                )
            )
        }
    }

    fun deletePreferences(userUUID: String) = preferencesDao.deletePreferences(userUUID)

    fun deletePreferences(domainPreferences: DomainPreferences) {
        if (domainPreferences.userUUID != null) {
            Timber.v("Deleting $domainPreferences")
            preferencesDao.deletePreferences(
                domainPreferences.toRoomModel(domainPreferences.userUUID)
            )
        } else {
            Timber.e(
                IllegalArgumentException(
                    "User UUID was null, which is the primary key.\n" +
                            "So it cannot be null and isn't in the SQLite database."
                )
            )
        }
    }

    fun setAnalyticsEnabled(userUUID: String, analyticsEnabled: Boolean) {
        logSet("analyticsEnabled", analyticsEnabled, userUUID)
        preferencesDao.setAnalyticsEnabled(userUUID, analyticsEnabled)
    }

    fun setShouldSync(userUUID: String, shouldSync: Boolean) {
        logSet("shouldSync", shouldSync, userUUID)
        preferencesDao.setShouldSync(userUUID, shouldSync)
    }

    fun setFreeDriveAutoStart(userUUID: String, freeDriveAutoStart: Boolean) {
        logSet("freeDriveAutoStart", freeDriveAutoStart, userUUID)
        preferencesDao.setFreeDriveAutoStart(userUUID, freeDriveAutoStart)
    }

    fun setShowAds(userUUID: String, showAds: Boolean) {
        logSet("showAds", showAds, userUUID)
        preferencesDao.setShowAds(userUUID, showAds)
    }

    fun setTheme(userUUID: String, theme: Theme) {
        logSet("theme", theme, userUUID)
        preferencesDao.setTheme(userUUID, theme)
    }

    fun setDynamicColorEnabled(userUUID: String, dynamicColorEnabled: Boolean) {
        logSet("dynamicColorEnabled", dynamicColorEnabled, userUUID)
        preferencesDao.setDynamicColorEnabled(userUUID, dynamicColorEnabled)
    }

    private fun logSet(name: String, value: Any, userUUID: String) {
        Timber.v("Setting $name to $value for user ${userUUID.take(4)}")
    }
}