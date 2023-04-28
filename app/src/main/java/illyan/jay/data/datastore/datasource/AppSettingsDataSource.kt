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

package illyan.jay.data.datastore.datasource

import androidx.datastore.core.DataStore
import illyan.jay.data.datastore.model.AppSettings
import illyan.jay.domain.model.DomainPreferences
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsDataSource @Inject constructor(
    private val appSettingsDataStore: DataStore<AppSettings>
) {
    val appSettings by lazy {
        appSettingsDataStore.data.map { settings ->
            if (settings.clientUUID == null) {
                val newSettings = settings.copy(clientUUID = UUID.randomUUID().toString())
                updateAppSettings { newSettings }
                newSettings
            } else {
                settings
            }
        }
    }

    suspend fun updateAppSettings(transform: (AppSettings) -> AppSettings) {
        appSettingsDataStore.updateData {
            val newSettings = transform(it)
            Timber.v("Changed settings from $it to $newSettings")
            newSettings
        }
    }

    suspend fun updateAppPreferences(transform: (DomainPreferences) -> DomainPreferences) {
        Timber.v("Updating App Preferences requested")
        updateAppSettings {
            it.copy(preferences = transform(it.preferences).copy(lastUpdate = ZonedDateTime.now()))
        }
    }
}