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
import illyan.jay.data.DataStatus
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
import kotlinx.coroutines.flow.update
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
            if (value != null && isLoading.value == false) {
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
            if (value != null && isLoading.value == false) {
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

    var showAds: Boolean?
        get() = userPreferences.value?.showAds
        set(value) {
            Timber.v("ShowAds preference change requested to $value")
            if (value != null && isLoading.value == false) {
                coroutineScopeIO.launch {
                    if (authInteractor.isUserSignedIn) {
                        preferencesDiskDataSource.setShowAds(authInteractor.userUUID!!, value)
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
            if (value != null && isLoading.value == false) {
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
            localUserPreferences,
            cloudPreferencesStatus
        ) { loading, local, syncedStatus ->
            if (loading == false && syncedStatus.isLoading == false) {
                local == syncedStatus.data
            } else {
                false
            }
        }.stateIn(
            coroutineScopeIO,
            SharingStarted.Eagerly,
            localUserPreferences.value == cloudPreferencesStatus.value.data
        )
    }

    val shouldSyncPreferences by lazy {
        localUserPreferences.map {
            it?.shouldSync ?: DomainPreferences.Default.shouldSync
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, DomainPreferences.Default.shouldSync)
    }

    val canSyncPreferences by lazy {
        combine(
            authInteractor.isUserSignedInStateFlow,
            isLoading,
            localUserPreferences
        ) { userSignedIn, loading, local ->
            userSignedIn && loading == false && local != null
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, false)
    }

    private val _localUserPreferences = MutableStateFlow<DomainPreferences?>(null)
    val localUserPreferences = _localUserPreferences.asStateFlow()


    private val _isLocalLoading = MutableStateFlow<Boolean?>(null)
    val isLocalLoading: StateFlow<Boolean?> = _isLocalLoading.asStateFlow()

    val cloudPreferencesStatus = preferencesNetworkDataSource.cloudPreferencesStatus

    val isLoading = combine(
        cloudPreferencesStatus,
        isLocalLoading
    ) { status, loadingFromDisk ->
        if (status.isLoading == null && loadingFromDisk == null) {
            null
        } else {
            status.isLoading == true || loadingFromDisk == true
        }
    }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

    init {
        _isLocalLoading.value = true
        coroutineScopeIO.launch {
            authInteractor.userUUIDStateFlow.collectLatest { uuid ->
                if (uuid != null) { // User signed in
                    coroutineScopeIO.launch {
                        preferencesDiskDataSource.getPreferences(uuid).collectLatest {
                            _localUserPreferences.value = it
                            if (_isLocalLoading.value != false) _isLocalLoading.update { false }
                        }
                    }
                } else { // Offline user
                    // Simple, we only use the baseline preferences for offline users
                    coroutineScopeIO.launch {
                        appSettingsFlow.collectLatest {
                            _localUserPreferences.value = it.preferences
                            if (_isLocalLoading.value != false) _isLocalLoading.update { false }
                        }
                    }
                }
            }
        }
    }

    val userPreferences by lazy {
        combine(
            cloudPreferencesStatus,
            localUserPreferences,
            authInteractor.isUserSignedInStateFlow,
            isLocalLoading,
        ) { flows ->
            val syncedStatus = flows[0] as DataStatus<DomainPreferences>
            val localPreferences = flows[1] as DomainPreferences?
            val isUserSignedIn = flows[2] as Boolean
            val isLocalLoading = flows[3] as Boolean?

            resolvePreferences(
                syncedPreferences = syncedStatus.data,
                localPreferences = localPreferences,
                isUserSignedIn = isUserSignedIn,
                isLocalLoading = isLocalLoading,
                isSyncLoading = syncedStatus.isLoading
            )
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    /**
     * Resolves the user preferences based on the current state of the app and the user's data.
     * While either local or synced preferences are loading, preferences will be null. If local
     * preferences are loaded, the resolved preferences will be local, but if synced preferences
     * are loaded and local preferences are still loading, the resolved preferences will be synced.
     * If the user changes preferences while either local or synced preferences are loading, the
     * requests will be ignored until both local and synced preferences are loaded and resolved.
     *
     * Generated by ChatGPT
     *
     * @param syncedPreferences The user's preferences stored in the cloud.
     * @param localPreferences The user's preferences stored on the device.
     * @param isUserSignedIn Whether the user is currently signed in.
     * @param isLocalLoading Whether local preferences are currently loading.
     * @param isSyncLoading Whether synced preferences are currently loading.
     * @return The resolved user preferences.
     * If both local and synced preferences are null, null will be returned.
     *
     * If local preferences are null and synced preferences are not, the resolved preferences will be the
     * synced preferences. If local preferences are not null and synced preferences are null, the resolved
     * preferences will be the local preferences. If both local and synced preferences are not null, the
     * resolved preferences will be based on which version is more fresh. If synced preferences are more fresh,
     * local preferences will be updated to match. If local preferences are more fresh, synced preferences
     * will be updated to match.
     */
    private fun resolvePreferences(
        syncedPreferences: DomainPreferences?,
        localPreferences: DomainPreferences?,
        isUserSignedIn: Boolean,
        isLocalLoading: Boolean?,
        isSyncLoading: Boolean?,
    ): DomainPreferences? {
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

        return if (isUserSignedIn) {
            if (isLocalLoading != false && isSyncLoading != false) { // While either is loading, preferences are null
                Timber.v("While local or synced preferences are loading, returning null")
                null
            } else if (isLocalLoading == false && isSyncLoading != false) { // If local is loaded, the preferences are local, cloud still loading
                Timber.v("If local is loaded and cloud is not, returning local preferences")
                localPreferences
            } else if (isLocalLoading != false && isSyncLoading == false) { // If cloud is loaded, the preferences are cloud, local still loading
                Timber.v("If cloud is loaded and local is not, returning cloud preferences")
                syncedPreferences
            } else {
                if (localPreferences == null && syncedPreferences == null) {
                    // User don't have local nor synced preferences? Create and upload local preferences.
                    Timber.v("User doesn't have local nor synced preferences, create and upload one")
                    val freshPreferences = DomainPreferences(userUUID = authInteractor.userUUID)
                    preferencesDiskDataSource.upsertPreferences(freshPreferences)
                    preferencesNetworkDataSource.setPreferences(freshPreferences)
                    null
                } else if (localPreferences == null && syncedPreferences != null) {
                    // User don't have local but have synced Preferences? Use synced preferences.
                    Timber.v("User doesn't have local but have synced preferences, save synced preferences")
                    preferencesDiskDataSource.upsertPreferences(syncedPreferences)
                    syncedPreferences
                } else if (localPreferences != null && localPreferences.shouldSync && syncedPreferences == null) {
                    // User have local but not synced preferences? Upload local preferences.
                    Timber.v("User has local preferences which need to be synced but has no preferences in cloud, upload local preferences")
                    preferencesNetworkDataSource.setPreferences(localPreferences)
                    localPreferences
                } else { // Both sessions are now loaded in and not null
                    if (!localPreferences!!.shouldSync) {
                        localPreferences
                    } else {
                        if (localPreferences == syncedPreferences!!) {
                            // Same lastUpdate, assuming the version is the same
                            Timber.v("Both local and synced preferences are matching, returning localPreferences")
                            localPreferences
                        } else if (localPreferences.isAfter(syncedPreferences)) {
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
            Timber.v("User not signed in, returning local app preferences.")
            localPreferences
        }
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