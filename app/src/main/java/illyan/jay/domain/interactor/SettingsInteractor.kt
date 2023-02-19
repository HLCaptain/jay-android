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

package illyan.jay.domain.interactor

import androidx.datastore.core.DataStore
import illyan.jay.data.disk.model.AppSettings
import illyan.jay.data.network.datasource.PreferencesNetworkDataSource
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsInteractor @Inject constructor(
    private val appSettingsDataStore: DataStore<AppSettings>,
    private val preferencesNetworkDataSource: PreferencesNetworkDataSource
) {
    val appSettingsFlow = appSettingsDataStore.data

    // TODO: store local settings for each user
    val userSettings by lazy {
        combine(
            appSettingsFlow,
            preferencesNetworkDataSource.preferences
        ) { appSettings, preferences ->

        }
    }

    suspend fun updateAppSettings(transform: (AppSettings) -> AppSettings) {
        appSettingsDataStore.updateData {
            transform(it)
        }
    }
}