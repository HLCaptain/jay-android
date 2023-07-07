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

import illyan.jay.data.datastore.datasource.AppSettingsDataSource
import illyan.jay.data.resolver.PreferencesResolver
import illyan.jay.data.room.datasource.PreferencesRoomDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.model.DomainPreferences
import illyan.jay.domain.model.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsInteractor @Inject constructor(
    private val appSettingsDataSource: AppSettingsDataSource,
    private val preferencesResolver: PreferencesResolver,
    private val preferencesRoomDataSource: PreferencesRoomDataSource,
    private val authInteractor: AuthInteractor,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    val appSettings = appSettingsDataSource.appSettings

    var freeDriveAutoStart: Boolean?
        get() = userPreferences.value?.freeDriveAutoStart
        set(value) {
            Timber.v("FreeDriveAutoStart preference change requested to $value")
            if (value != null && preferencesResolver.isLoading.value == false) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesRoomDataSource.setFreeDriveAutoStart(authInteractor.userUUID!!, value)
                    } else {
                        appSettingsDataSource.updateAppPreferences { it.copy(freeDriveAutoStart = value) }
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
                        appSettingsDataSource.updateAppPreferences {
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
                        appSettingsDataSource.updateAppPreferences {
                            it.copy(showAds = value)
                        }
                    }
                }
            }
        }

    var theme: Theme?
        get() = userPreferences.value?.theme
        set(value) {
            Timber.v("Theme preference change requested to $value")
            if (value != null && preferencesResolver.isLoading.value == false) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesRoomDataSource.setTheme(authInteractor.userUUID!!, value)
                    } else {
                        appSettingsDataSource.updateAppPreferences {
                            it.copy(theme = value)
                        }
                    }
                }
            }
        }

    var dynamicColorEnabled: Boolean?
        get() = userPreferences.value?.dynamicColorEnabled
        set(value) {
            Timber.v("DynamicColorEnabled preference change requested to $value")
            if (value != null && preferencesResolver.isLoading.value == false) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesRoomDataSource.setDynamicColorEnabled(authInteractor.userUUID!!, value)
                    } else {
                        appSettingsDataSource.updateAppPreferences {
                            it.copy(dynamicColorEnabled = value)
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

    /**
     * Automatically updates [DomainPreferences.lastUpdate]
     */

    fun deleteLocalUserPreferences() {
        if (authInteractor.isUserSignedIn) {
            preferencesRoomDataSource.deletePreferences(authInteractor.userUUID!!)
        }
    }
}