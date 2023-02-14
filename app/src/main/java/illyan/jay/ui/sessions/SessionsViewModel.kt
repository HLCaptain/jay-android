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

package illyan.jay.ui.sessions

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.di.CoroutineDispatcherIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import illyan.jay.domain.model.DomainSession
import illyan.jay.ui.sessions.model.UiSession
import illyan.jay.ui.sessions.model.toUiModel
import illyan.jay.util.sphericalPathLength
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionInteractor: SessionInteractor,
    private val locationInteractor: LocationInteractor,
    private val authInteractor: AuthInteractor,
    private val settingsInteractor: SettingsInteractor,
    @CoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    private val sessionStateFlows = mutableMapOf<String, MutableStateFlow<UiSession?>>()

    // FIXME: deleteRequestedOnSessions may be aware of sessions's sync state, so
    //  they would only be present in this list, if their deletion is pending
    private val deleteRequestedOnSessions = MutableStateFlow(persistentListOf<String>())

    private val _ownedLocalSessionUUIDs = MutableStateFlow(listOf<Pair<String, ZonedDateTime>>())
    val ownedLocalSessionUUIDs = _ownedLocalSessionUUIDs.asStateFlow()

    val isUserSignedIn = authInteractor.isUserSignedInStateFlow
    val signedInUser = authInteractor.currentUserStateFlow

    private val _syncedSessionsLoading = MutableStateFlow(false)
    val syncedSessionsLoading = _syncedSessionsLoading.asStateFlow()

    private val _localSessionsLoading = MutableStateFlow(false)
    val localSessionsLoading = _localSessionsLoading.asStateFlow()

    private val clientUUID = settingsInteractor.appSettingsFlow.map { it.clientUUID ?: "" }
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
            if (sessionFlow.value?.isSynced != isSynced) {
                sessionFlow.value = sessionFlow.value?.copy(isSynced = isSynced)
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

    fun reloadData(activity: Activity) {
        Timber.v("Requested data reload")
        disposeSessionStateFlows()
        loadLocalSessions()
        loadCloudSessions(activity)
        loadSessionStateFlows()
    }

    fun loadLocalSessions() {
        _notOwnedSessionUUIDs.value = emptyList()
        _ongoingSessionUUIDs.value = emptyList()
        _ownedLocalSessionUUIDs.value = emptyList()
        _localSessionsLoading.value = true
        val loadingOwnedSessions = MutableStateFlow(true)
        val loadingOngoingSessions = MutableStateFlow(true)
        val loadingNotOwnedSessions = MutableStateFlow(true)

        viewModelScope.launch(dispatcherIO) {
            combine(
                loadingOwnedSessions,
                loadingNotOwnedSessions,
                loadingOngoingSessions
            ) { owned, notOwned, ongoing ->
                !owned && !notOwned && !ongoing
            }.collectLatest {
                if (it) _localSessionsLoading.value = false
            }
        }

        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.getNotOwnedSessions().collectLatest { sessions ->
                _notOwnedSessionUUIDs.value = sessions.map { it.uuid to it.startDateTime }
                Timber.d("Got ${sessions.size} not owned sessions")
                loadingNotOwnedSessions.value = false
            }
        }
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.getOngoingSessionUUIDs().collectLatest {
                _ongoingSessionUUIDs.value = it
                Timber.d("Got ${it.size} ongoing sessions")
                loadingOngoingSessions.value = false
            }
        }
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.getOwnSessions().collectLatest { sessions ->
                _ownedLocalSessionUUIDs.value = sessions.map { it.uuid to it.startDateTime }
                Timber.d("Got ${sessions.size} owned sessions by ${signedInUser.value?.uid}")
                loadingOwnedSessions.value = false
            }
        }
    }

    fun loadCloudSessions(activity: Activity) {
        _syncedSessions.value = emptyList()
        _syncedSessionsLoading.value = true

        if (isUserSignedIn.value) {
            viewModelScope.launch(dispatcherIO) {
                sessionInteractor.reloadSyncedSessionsForCurrentUser(activity)
                sessionInteractor.syncedSessions.collectLatest {
                    Timber.d("New number of synced sessions: ${_syncedSessions.value.size} -> ${it?.size}")
                    _syncedSessions.value = it ?: emptyList()
                    _syncedSessionsLoading.value = it == null
                }
            }
        } else {
            _syncedSessionsLoading.value = false
        }
    }

    fun deleteAllSyncedData() {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.deleteAllSyncedData()
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
            sessionInteractor.getSession(uuid).first { session ->
                session?.let {
                    locationInteractor.getLocations(uuid).first { locations ->
                        sessionInteractor.uploadSession(session, locations)
                        true
                    }
                }
                true
            }
        }
    }

    fun deleteSession(uuid: String) {
        deleteRequestedOnSessions.getAndUpdate { it.add(uuid) }
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
            sessionInteractor.getSession(uuid).first {
                it?.let { sessionInteractor.deleteSessionLocally(it) }
                true
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

    fun disposeSessionStateFlows() {
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
        Timber.d("Requesting session state flow with id: ${sessionUUID.take(4)}")
        if (sessionStateFlows[sessionUUID] != null) {
            Timber.d("Session flow for session ${sessionUUID.take(4)} found in memory")
            return sessionStateFlows[sessionUUID]!!.asStateFlow()
        }
        val sessionMutableStateFlow = MutableStateFlow<UiSession?>(null)
        sessionStateFlows[sessionUUID] = sessionMutableStateFlow
        Timber.v("Session flow not found, creating new one for ${sessionUUID.take(4)}")

        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.getSession(sessionUUID).collectLatest { session ->
                if (session != null) {
                    sessionMutableStateFlow.value = session.toUiModel(
                        currentClientUUID = clientUUID.value,
                        isLocal = sessionStateFlows[sessionUUID]?.value?.isLocal ?: false,
                        isSynced = syncedSessions.value.any { it.uuid == sessionUUID }
                    )
                } else {
                    val remoteSession = syncedSessions.value.firstOrNull { it.uuid == sessionUUID }
                    sessionMutableStateFlow.value = remoteSession
                    if (remoteSession != null) {
                        Timber.d("Session ${sessionUUID.take(4)} found in cloud")
                    } else {
                        Timber.d("Session ${sessionUUID.take(4)} not found, returning null")
                    }
                }
            }
        }
        viewModelScope.launch(dispatcherIO) {
            locationInteractor.getLocations(sessionUUID).collectLatest { locations ->
                val storingLocations = locations.isNotEmpty()
                val totalDistance = if (storingLocations) {
                    locations.sphericalPathLength()
                } else {
                    sessionMutableStateFlow.value?.totalDistance ?: -1.0
                }
                if (sessionMutableStateFlow.value != null) {
                    sessionMutableStateFlow.value = sessionMutableStateFlow.value?.copy(
                        totalDistance = totalDistance,
                        isLocal = storingLocations,
                    )
                }
                sessionMutableStateFlow.collectLatest { session ->
                    sessionMutableStateFlow.value = session?.copy(
                        totalDistance = if (storingLocations) totalDistance else session.totalDistance,
                        isLocal = storingLocations,
                    )
                }
            }
        }
        return sessionMutableStateFlow.asStateFlow()
    }
}