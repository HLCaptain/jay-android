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
import illyan.jay.data.datastore.model.AppSettings
import illyan.jay.data.resolver.PreferencesResolver
import illyan.jay.data.room.datasource.PreferencesRoomDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.model.DomainPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsInteractor @Inject constructor(
    private val appSettingsDataStore: DataStore<AppSettings>,
    private val preferencesResolver: PreferencesResolver,
    private val preferencesRoomDataSource: PreferencesRoomDataSource,
    private val authInteractor: AuthInteractor,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    val appSettingsFlow = appSettingsDataStore.data.map { settings ->
        if (settings.clientUUID == null) {
            val newSettings = settings.copy(clientUUID = UUID.randomUUID().toString())
            updateAppSettings { newSettings }
            newSettings
        } else {
            settings
        }
    }

    var freeDriveAutoStart: Boolean?
        get() = userPreferences.value?.freeDriveAutoStart
        set(value) {
            Timber.v("FreeDriveAutoStart preference change requested to $value")
            if (value != null && preferencesResolver.isLoading.value == false) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesRoomDataSource.setFreeDriveAutoStart(authInteractor.userUUID!!, value)
                    } else {
                        updateAppPreferences { it.copy(freeDriveAutoStart = value) }
                    }
                }
            }
        }

    var analyticsEnabled: Boolean?
        get() = userPreferences.value?.analyticsEnabled
        set(value) {
            Timber.v("AnalyticsEnabled preference change requested to $value")
            if (value != null && preferencesResolver.isLoading.value == false) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        Timber.v("Setting Analytics $value for signed in user")
                        preferencesRoomDataSource.setAnalyticsEnabled(authInteractor.userUUID!!, value)
                    } else {
                        Timber.v("Setting Analytics $value for offline user")
                        updateAppPreferences {
                            it.copy(
                                analyticsEnabled = value,
                                lastUpdateToAnalytics = ZonedDateTime.now()
                            )
                        }
                    }
                }
            }
        }

    var showAds: Boolean?
        get() = userPreferences.value?.showAds
        set(value) {
            Timber.v("ShowAds preference change requested to $value")
            if (value != null && preferencesResolver.isLoading.value == false) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesRoomDataSource.setShowAds(authInteractor.userUUID!!, value)
                    } else {
                        updateAppPreferences {
                            it.copy(showAds = value)
                        }
                    }
                }
            }
        }

    var shouldSync: Boolean?
        get() = userPreferences.value?.shouldSync
        set(value) {
            Timber.v("ShouldSync preference change requested to $value")
            if (value != null && preferencesResolver.isLoading.value == false) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesRoomDataSource.setShouldSync(authInteractor.userUUID!!, value)
                    }
                }
            }
        }

    val arePreferencesSynced = preferencesResolver.isDataSynced
    val shouldSyncPreferences = preferencesResolver.shouldSyncData
    val canSyncPreferences = preferencesResolver.canSyncData
    val localUserPreferences = preferencesResolver.localData
    val userPreferences = preferencesResolver.data

    suspend fun updateAppSettings(transform: (AppSettings) -> AppSettings) {
        appSettingsDataStore.updateData {
            val newSettings = transform(it)
            Timber.v("Changed settings from $it to $newSettings")
            newSettings
        }
    }

    /**
     * Automatically updates [DomainPreferences.lastUpdate]
     */
    suspend fun updateAppPreferences(transform: (DomainPreferences) -> DomainPreferences) {
        updateAppSettings {
            it.copy(preferences = transform(it.preferences).copy(lastUpdate = ZonedDateTime.now()))
        }
    }

    fun deleteLocalUserPreferences() {
        if (authInteractor.isUserSignedIn) {
            preferencesRoomDataSource.deletePreferences(authInteractor.userUUID!!)
        }
    }
}