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

package illyan.jay.data.resolver

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import illyan.jay.data.DataStatus
import illyan.jay.data.datastore.datasource.AppSettingsDataSource
import illyan.jay.data.firestore.datasource.PreferencesFirestoreDataSource
import illyan.jay.data.room.datasource.PreferencesRoomDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class PreferencesResolver @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val appSettingsDataSource: AppSettingsDataSource,
    private val preferencesFirestoreDataSource: PreferencesFirestoreDataSource,
    private val preferencesRoomDataSource: PreferencesRoomDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
    connectivityManager: ConnectivityManager,
) : DataResolver<DomainPreferences>(
    coroutineScopeIO = coroutineScopeIO
) {
    val isConnectedToInternet = MutableStateFlow(false)
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    private val networkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) { isConnectedToInternet.update { true } }
        override fun onLost(network: Network) { isConnectedToInternet.update { false } }
    }
    init {
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override val enableSyncedData = combine(
        authInteractor.isUserSignedInStateFlow,
        isConnectedToInternet,
    ) { isUserSignedIn, isConnectedToInternet ->
        isUserSignedIn && isConnectedToInternet
    }

    override val cloudDataStatus = preferencesFirestoreDataSource.cloudPreferencesStatus

    override val localDataStatus: StateFlow<DataStatus<DomainPreferences>> by lazy {
        val statusStateFlow = MutableStateFlow(DataStatus<DomainPreferences>())
        Timber.v("Refreshing local user preferences data collection")
        var dataCollectionJob: Job?
        coroutineScopeIO.launch {
            dataCollectionJob = refreshLocalPreferences(authInteractor.userUUID, statusStateFlow)
            authInteractor.userUUIDStateFlow.collectLatest { uuid ->
                dataCollectionJob?.cancel(CancellationException("User Authentication changed, need to cancel jobs depending on User Authentication"))
                dataCollectionJob = refreshLocalPreferences(uuid, statusStateFlow)
            }
        }
        statusStateFlow.asStateFlow()
    }

    private suspend fun refreshLocalPreferences(
        uuid: String?,
        statusStateFlow: MutableStateFlow<DataStatus<DomainPreferences>>,
    ): Job {
        statusStateFlow.update { DataStatus(data = null, isLoading = true) }
        return if (uuid != null) { // User signed in
            Timber.v("Collecting signed in user preferences from disk")
            coroutineScopeIO.launch {
                preferencesRoomDataSource.getPreferences(uuid).collectLatest { preferences ->
                    statusStateFlow.update { DataStatus(preferences, false) }
                }
            }
        } else { // Offline user
            Timber.v("Collecting offline user preferences from disk")
            // Simple, we only use the baseline preferences for offline users
            coroutineScopeIO.launch {
                appPreferences.collectLatest { preferences ->
                    statusStateFlow.update { DataStatus(preferences, false) }
                }
            }
        }
    }

    val appPreferences by lazy { appSettingsDataSource.appSettings.map { it.preferences } }

    override fun shouldSyncData(localData: DomainPreferences?): Boolean {
        return localData?.shouldSync ?: DomainPreferences.Default.shouldSync
    }

    override fun uploadDataToCloud(data: DomainPreferences) {
        preferencesFirestoreDataSource.setPreferences(data)
    }

    override fun upsertDataToLocal(data: DomainPreferences) {
        preferencesRoomDataSource.upsertPreferences(data)
    }

    override fun createNewDataInstance(): DomainPreferences {
        return DomainPreferences(userUUID = authInteractor.userUUID)
    }

    override fun resolve(
        localData: DomainPreferences,
        syncedData: DomainPreferences,
    ): ResolvedState {
        return if (localData == syncedData) {
            // Same lastUpdate, assuming the version is the same
            Timber.v("Both local and synced preferences are matching, returning localPreferences")
            ResolvedState.Equal
        } else if (localData.isAfter(syncedData)) {
            // If local is more fresh, then update synced preferences.
            Timber.v("Local preferences are more fresh, uploading it to cloud")
            ResolvedState.Local
        } else {
            // If synced is more fresh, then update local preferences.
            Timber.v("Synced preferences are more fresh, saving it onto disk")
            ResolvedState.Synced
        }
    }
}