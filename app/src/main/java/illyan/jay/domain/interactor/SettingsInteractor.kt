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
import illyan.jay.data.disk.datasource.PreferencesDiskDataSource
import illyan.jay.data.disk.model.AppSettings
import illyan.jay.data.network.datasource.PreferencesNetworkDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.model.DomainPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsInteractor @Inject constructor(
    private val appSettingsDataStore: DataStore<AppSettings>,
    private val preferencesNetworkDataSource: PreferencesNetworkDataSource,
    private val preferencesDiskDataSource: PreferencesDiskDataSource,
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
            if (value != null && !isLoading.value) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesDiskDataSource.setFreeDriveAutoStart(authInteractor.userUUID!!, value)
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
            if (value != null && !isLoading.value) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesDiskDataSource.setAnalyticsEnabled(authInteractor.userUUID!!, value)
                    } else {
                        updateAppPreferences {
                            it.copy(analyticsEnabled = value)
                        }
                    }
                }
            }
        }

    var shouldSync: Boolean?
        get() = userPreferences.value?.shouldSync
        set(value) {
            Timber.v("ShouldSync preference change requested to $value")
            if (value != null && !isLoading.value) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesDiskDataSource.setShouldSync(authInteractor.userUUID!!, value)
                    }
                }
            }
        }

    val arePreferencesSynced by lazy {
        combine(
            isLoading,
            preferencesNetworkDataSource.isLoadingFromCloud,
            localUserPreferences,
            syncedUserPreferences
        ) { loading, cloudLoading, local, synced ->
            if (!loading && !cloudLoading) {
                local == synced
            } else {
                false
            }
        }.stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            localUserPreferences.value == syncedUserPreferences.value
        )
    }

    val shouldSyncPreferences by lazy {
        localUserPreferences.map {
            it?.shouldSync ?: DomainPreferences.default.shouldSync
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, DomainPreferences.default.shouldSync)
    }

    val canSyncPreferences by lazy {
        combine(
            authInteractor.isUserSignedInStateFlow,
            isLoading,
            localUserPreferences
        ) { userSignedIn, loading, local ->
            userSignedIn && !loading && local != null
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, false)
    }

    private val _localUserPreferences = MutableStateFlow<DomainPreferences?>(null)
    val localUserPreferences = _localUserPreferences.asStateFlow()

    val isSyncLoading = preferencesNetworkDataSource.isLoading

    private val _isLocalLoading = MutableStateFlow(false)
    val isLocalLoading: StateFlow<Boolean>
        get() {
            return _isLocalLoading.asStateFlow()
        }

    val syncedUserPreferences = preferencesNetworkDataSource.cloudPreferences

    val isLoading = combine(
        preferencesNetworkDataSource.isLoading,
        isLocalLoading
    ) { loadingFromCache, loadingFromDisk ->
        loadingFromCache || loadingFromDisk
    }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, false)

    init {
        _isLocalLoading.value = true
        coroutineScopeIO.launch {
            authInteractor.userUUIDStateFlow.collectLatest { uuid ->
                if (uuid != null) { // User signed in
                    coroutineScopeIO.launch {
                        preferencesDiskDataSource.getPreferences(uuid).collectLatest {
                            _localUserPreferences.value = it
                            if (_isLocalLoading.value) _isLocalLoading.value = false
                        }
                    }
                } else { // Offline user
                    // Simple, we only use the baseline preferences for offline users
                    coroutineScopeIO.launch {
                        appSettingsFlow.collectLatest {
                            _localUserPreferences.value = it.preferences
                            if (_isLocalLoading.value) _isLocalLoading.value = false
                        }
                    }
                }
            }
        }
    }
    // TODO: store local settings for each user
    val userPreferences by lazy {
        combine(
            syncedUserPreferences,
            localUserPreferences,
            authInteractor.isUserSignedInStateFlow,
            isLocalLoading,
            isSyncLoading
        ) { flows ->
            val syncedPreferences = flows[0] as DomainPreferences?
            val localPreferences = flows[1] as DomainPreferences?
            val isUserSignedIn = flows[2] as Boolean
            val isLocalLoading = flows[3] as Boolean
            val isSyncedLoading = flows[4] as Boolean

            // While either is loading, preferences are null
            // If local is loaded, the preferences are local, cloud still loading
            // If cloud is loaded, the preferences are cloud, local still loading
            // If user changes preferences when either is loading, resolve requests (easier to ignore requests) when both are loaded and resolved.
            // ONLY RESOLVE ANYTHING WHEN BOTH CLOUD AND LOCAL IS LOADED (create new preferences document)

            // User don't have local nor synced preferences? Create and upload local preferences.
            // User don't have local but have synced Preferences? Use synced preferences.
            // User have local but not synced preferences? Upload local preferences.
            // User have local and synced preferences, which to use?
            // If synced is more fresh, then update local preferences.
            // If local is more fresh, then update synced version. (EASIER)

            if (isUserSignedIn) {
                if (isLocalLoading && isSyncedLoading) { // While either is loading, preferences are null
                    Timber.v("While local or synced preferences are loading, returning null")
                    null
                } else if (!isLocalLoading && isSyncedLoading) { // If local is loaded, the preferences are local, cloud still loading
                    Timber.v("If local is loaded and cloud is not, returning local preferences")
                    localPreferences
                } else if (isLocalLoading && !isSyncedLoading) { // If cloud is loaded, the preferences are cloud, local still loading
                    Timber.v("If cloud is loaded and local is not, returning cloud preferences")
                    syncedPreferences
                } else {
                    if (localPreferences == null && syncedPreferences == null) {
                        // User don't have local nor synced preferences? Create and upload local preferences.
                        Timber.v("User don't have local nor synced preferences, create and upload one.")
                        val freshPreferences = DomainPreferences(userUUID = authInteractor.userUUID)
                        preferencesDiskDataSource.upsertPreferences(freshPreferences)
                        preferencesNetworkDataSource.setPreferences(freshPreferences)
                        null
                    } else if (localPreferences == null && syncedPreferences != null) {
                        // User don't have local but have synced Preferences? Use synced preferences.
                        Timber.v("User don't have local but have synced preferences, save synced preferences.")
                        preferencesDiskDataSource.upsertPreferences(syncedPreferences)
                        syncedPreferences
                    } else if (localPreferences != null && syncedPreferences == null) {
                        // User have local but not synced preferences? Upload local preferences.
                        Timber.v("User have local but not synced preferences, upload local preferences.")
                        preferencesNetworkDataSource.setPreferences(localPreferences)
                        localPreferences
                    } else { // Both sessions are now loaded in and not null
                        if (!localPreferences!!.shouldSync) {
                            localPreferences
                        } else {
                            if (localPreferences == syncedPreferences) {
                                // Same lastUpdate, assuming the version is the same
                                Timber.v("Both local and synced preferences' lastUpdate are matching. Assuming they are the same, returning localPreferences.")
                                localPreferences
                            } else if (
                                localPreferences.lastUpdate.toInstant().epochSecond >
                                syncedPreferences!!.lastUpdate.toInstant().epochSecond
                            ) {
                                // If local is more fresh, then update synced preferences.
                                Timber.v("Local preferences are more fresh, uploading it to cloud")
                                preferencesNetworkDataSource.setPreferences(localPreferences)
                                localPreferences
                            } else {
                                // If synced is more fresh, then update local preferences.
                                Timber.v("Synced preferences are more fresh, saving it onto disk")
                                preferencesDiskDataSource.upsertPreferences(syncedPreferences)
                                syncedPreferences
                            }
                        }
                    }
                }
            } else {
                Timber.v("User not signed in, returning local app preferences")
                localPreferences
            }
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

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
}