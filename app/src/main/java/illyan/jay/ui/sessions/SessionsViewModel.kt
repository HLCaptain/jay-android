/*
 * Copyright (c) 2022-2024 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.data.datastore.datasource.AppSettingsDataSource
import illyan.jay.di.CoroutineDispatcherIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SensorEventInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainSession
import illyan.jay.ui.sessions.model.UiSession
import illyan.jay.ui.sessions.model.toUiModel
import illyan.jay.util.awaitOperations
import illyan.jay.util.sphericalPathLength
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionInteractor: SessionInteractor,
    private val locationInteractor: LocationInteractor,
    private val sensorEventInteractor: SensorEventInteractor,
    authInteractor: AuthInteractor,
    appSettingsDataSource: AppSettingsDataSource,
    @CoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    private val sessionStateFlows = mutableMapOf<String, MutableStateFlow<UiSession?>>()

    // FIXME: deleteRequestedOnSessions may be aware of sessions's sync state, so
    //  they would only be present in this list, if their deletion is pending
    // FIXME: create a list with not yet synced sessions with the cloud (local cache vs fresh cloud data)
    private val deleteRequestedOnSessions = MutableStateFlow(persistentListOf<String>())

    private val _ownedLocalSessionUUIDs = MutableStateFlow(listOf<Pair<String, ZonedDateTime>>())
    val ownedLocalSessionUUIDs = _ownedLocalSessionUUIDs.asStateFlow()

    val isUserSignedIn = authInteractor.isUserSignedInStateFlow
    val signedInUser = authInteractor.userStateFlow

    private val _syncedSessionsLoading = MutableStateFlow(false)
    val syncedSessionsLoading = _syncedSessionsLoading.asStateFlow()

    private val _localSessionsLoading = MutableStateFlow(false)
    val localSessionsLoading = _localSessionsLoading.asStateFlow()

    private val clientUUID = appSettingsDataSource.appSettings.map { it.clientUUID ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _syncedSessions = MutableStateFlow(listOf<DomainSession>())
    val syncedSessions = combine(
        _syncedSessions,
        clientUUID
    ) { synced, clientUUID ->
        Timber.v("${synced.size} synced sessions")
        synced.map {
            it.toUiModel(
                currentClientUUID = clientUUID,
                isSynced = true,
                isLocal = sessionStateFlows[it.uuid]?.value?.isLocal ?: false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _notOwnedSessionUUIDs = MutableStateFlow(listOf<Pair<String, ZonedDateTime>>())

    val isLoading = combine(
        localSessionsLoading,
        syncedSessionsLoading
    ) { localLoading, syncedLoading ->
        localLoading || syncedLoading
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val notOwnedSessionUUIDs = _notOwnedSessionUUIDs.asStateFlow()
    val areThereSessionsNotOwned = notOwnedSessionUUIDs.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _ongoingSessionUUIDs = MutableStateFlow(listOf<String>())
    val ongoingSessionUUIDs = _ongoingSessionUUIDs.asStateFlow()

    val canDeleteSessionsLocally = combine(
        ownedLocalSessionUUIDs,
        notOwnedSessionUUIDs,
        ongoingSessionUUIDs,
    ) { owned, notOwned, ongoing ->
        owned.size + notOwned.size - ongoing.size > 0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val allSessionUUIDs = combine(
        syncedSessions,
        ownedLocalSessionUUIDs,
        notOwnedSessionUUIDs,
        deleteRequestedOnSessions,
    ) { synced, ownedLocal, notOwnedLocal, deleting ->
        val sessions = mutableListOf<Pair<String, ZonedDateTime>>()
        sessions.addAll(synced.map { it.uuid to it.startDateTime })
        sessions.addAll(ownedLocal)
        sessions.addAll(notOwnedLocal)
        val distinctSessions = sessions.distinct()
        val sortedSessions = distinctSessions
            .sortedByDescending { it.second.toInstant().toEpochMilli() }
            .map { it.first }
        sortedSessions.intersect(sessionStateFlows.keys).forEach { uuid ->
            val sessionFlow = sessionStateFlows[uuid]!!
            val isSynced = synced.any { it.uuid == uuid }
            sessionFlow.update {
                if (it?.isSynced != isSynced) {
                    it?.copy(isSynced = isSynced)
                } else {
                    it
                }
            }

        }
        // Session is not being deleted
        sortedSessions.filter { !deleting.contains(it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val noSessionsToShow = combine(
        allSessionUUIDs,
        isLoading
    ) { sessions, loading ->
        if (loading) {
            false
        } else {
            sessions.isEmpty()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val canSyncSessions = combine(
        syncedSessions,
        ownedLocalSessionUUIDs,
        ongoingSessionUUIDs
    ) { synced, owned, ongoing ->
        // There is at least one session which can be synced (not ongoing).
        // The number of local sessions in the cloud is lower than local not ongoing sessions.
        synced.size < owned.size - ongoing.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun reloadData() {
        Timber.v("Requested data reload")
        disposeSessionStateFlows()
        disposeJobs()
        loadLocalSessions()
        loadCloudSessions()
        loadSessionStateFlows()
    }

    private fun disposeJobs() {
        val cancellationException = CancellationException("Requested disposing data collection jobs currently running")
        realtimeDataCollectionJobs.forEach { it.cancel(cancellationException) }
        realtimeDataCollectionJobs.clear()
    }

    val realtimeDataCollectionJobs = mutableListOf<Job>()

    private fun loadLocalSessions() {
        _notOwnedSessionUUIDs.update { emptyList() }
        _ongoingSessionUUIDs.update { emptyList() }
        _ownedLocalSessionUUIDs.update { emptyList() }
        _localSessionsLoading.update { true }

        viewModelScope.launch(dispatcherIO) {
            // Only need 1 operation to consider local sessions "loaded"
            awaitOperations(1) { onOperationFinished ->
                realtimeDataCollectionJobs += viewModelScope.launch(dispatcherIO) {
                    sessionInteractor.getNotOwnedSessions().collectLatest { sessions ->
                        _notOwnedSessionUUIDs.update { sessions.map { it.uuid to it.startDateTime } }
                        Timber.d("Got ${sessions.size} not owned sessions")
                        onOperationFinished()
                    }
                }
                realtimeDataCollectionJobs += viewModelScope.launch(dispatcherIO) {
                    sessionInteractor.getOngoingSessionUUIDs().collectLatest { uuids ->
                        _ongoingSessionUUIDs.update { uuids }
                        Timber.d("Got ${uuids.size} ongoing sessions")
                        onOperationFinished()
                    }
                }
                realtimeDataCollectionJobs += viewModelScope.launch(dispatcherIO) {
                    sessionInteractor.getOwnSessions().collectLatest { sessions ->
                        _ownedLocalSessionUUIDs.update { sessions.map { it.uuid to it.startDateTime } }
                        Timber.d("Got ${sessions.size} owned sessions by ${signedInUser.value?.uid?.take(4)}")
                        onOperationFinished()
                    }
                }
            }
            _localSessionsLoading.update { false }
        }
    }

    fun loadCloudSessions() {
        _syncedSessions.update { emptyList() }
        _syncedSessionsLoading.update { true }

        if (isUserSignedIn.value) {
            realtimeDataCollectionJobs += viewModelScope.launch(dispatcherIO) {
                sessionInteractor.syncedSessions.collectLatest { sessions ->
                    Timber.d("New number of synced sessions: ${_syncedSessions.value.size} -> ${sessions?.size}")
                    _syncedSessions.update { sessions ?: emptyList() }
                    _syncedSessionsLoading.update { sessions == null }
                }
            }
        } else {
            _syncedSessionsLoading.update { false }
        }
    }

    fun deleteSyncedSessions() {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.deleteSyncedSessions()
        }
    }

    fun ownSession(sessionUUID: String) {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.ownSession(sessionUUID)
        }
    }

    fun ownAllSessions() {
        sessionInteractor.ownAllNotOwnedSessions()
    }

    fun syncSessions() {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.uploadNotSyncedSessions()
        }
    }

    fun syncSession(uuid: String) {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.getSession(uuid).first()?.let { session ->
                val locations = locationInteractor.getLocations(uuid).first()
                val events = sensorEventInteractor.getSensorEvents(uuid).first()
                val aggressions = locationInteractor.getAggressions(uuid).first()
                sessionInteractor.uploadSession(session, locations, events, aggressions)
            }
        }
    }

    fun deleteSession(uuid: String) {
        deleteRequestedOnSessions.update { it.add(uuid) }
        deleteSessionLocally(uuid)
        deleteSessionFromCloud(uuid)
    }

    fun deleteSessionFromCloud(uuid: String) {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.deleteSessionFromCloud(uuid)
        }
    }

    fun deleteSessionLocally(uuid: String) {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.getSession(uuid).first()?.let {
                sessionInteractor.deleteSessionLocally(it)
            }
//            loadLocalSessions()
        }
    }

    fun deleteSessionsLocally() {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.deleteStoppedSessions()
//            loadLocalSessions()
        }
    }

    fun disposeSessionStateFlow(sessionUUID: String) {
        // Disabled due to getting sessions one-by-one are not performant enough
        //sessionStateFlows.remove(sessionUUID)
    }

    private fun disposeSessionStateFlows() {
        sessionStateFlows.clear()
    }

    fun loadSessionStateFlows() {
        // FIXME: make loading sessions a constant time-like instead of O(n)
        //  (one query to DB and cloud instead of N)
        viewModelScope.launch(dispatcherIO) {
//            allSessionUUIDs.collectLatest { uuids ->
//                uuids.subtract(sessionStateFlows.keys).forEach { getSessionStateFlow(it) }
//            }
            combine(allSessionUUIDs, isLoading) { uuids, loading ->
                uuids to loading
            }.first { state ->
                state.first.subtract(sessionStateFlows.keys).forEach { getSessionStateFlow(it) }
                !state.second && state.first.isNotEmpty()
            }
        }
    }

    fun getSessionStateFlow(sessionUUID: String): StateFlow<UiSession?> {
        Timber.v("Requesting session state flow with id: ${sessionUUID.take(4)}")
        if (sessionStateFlows[sessionUUID] != null) {
            Timber.v("Session flow for session ${sessionUUID.take(4)} found in memory")
            return sessionStateFlows[sessionUUID]!!.asStateFlow()
        }
        val sessionMutableStateFlow = MutableStateFlow<UiSession?>(null)
        sessionStateFlows[sessionUUID] = sessionMutableStateFlow
        Timber.v("Session flow not found, creating new one for ${sessionUUID.take(4)}")

        realtimeDataCollectionJobs += viewModelScope.launch(dispatcherIO) {
            sessionInteractor.getSession(sessionUUID).collectLatest { session ->
                if (session != null) {
                    Timber.v("Session $sessionUUID isSynced = ${syncedSessions.value.any { it.uuid == sessionUUID }}")
                    sessionMutableStateFlow.update {
                        session.toUiModel(
                            currentClientUUID = clientUUID.value,
                            isLocal = sessionStateFlows[sessionUUID]?.value?.isLocal ?: false,
                            isSynced = syncedSessions.value.any { it.uuid == sessionUUID }
                        )
                    }
                } else {
                    val remoteSession = syncedSessions.value.firstOrNull { it.uuid == sessionUUID }
                    sessionMutableStateFlow.update { remoteSession }
                    if (remoteSession != null) {
                        Timber.v("Session ${sessionUUID.take(4)} found in cloud")
                    } else {
                        Timber.v("Session ${sessionUUID.take(4)} not found, returning null")
                    }
                }
            }
        }
        realtimeDataCollectionJobs += viewModelScope.launch(dispatcherIO) {
            locationInteractor.getLocations(sessionUUID).collectLatest { locations ->
                val storingLocations = locations.isNotEmpty()
                val totalDistance = if (storingLocations) {
                    locations.sphericalPathLength()
                } else {
                    sessionMutableStateFlow.value?.totalDistance
                }
                Timber.v("Session $sessionUUID isLocal = $storingLocations")
                sessionMutableStateFlow.first { session ->
                    if (session != null) {
                        sessionMutableStateFlow.update {
                            it?.copy(
                                totalDistance = totalDistance,
                                isLocal = storingLocations,
                            )
                        }
                    }
                    session != null
                }
            }
        }
        return sessionMutableStateFlow.asStateFlow()
    }
}