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
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import illyan.jay.domain.model.DomainSession
import illyan.jay.ui.sessions.model.UiSession
import illyan.jay.ui.sessions.model.toUiModel
import illyan.jay.util.sphericalPathLength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionInteractor: SessionInteractor,
    private val locationInteractor: LocationInteractor,
    private val authInteractor: AuthInteractor,
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {
    private val sessionStateFlows = mutableMapOf<String, MutableStateFlow<UiSession?>>()

    private val _ownedLocalSessionUUIDs = MutableStateFlow(listOf<String>())
    val ownedLocalSessionUUIDs = _ownedLocalSessionUUIDs.asStateFlow()

    val isUserSignedIn = authInteractor.isUserSignedInStateFlow
    val signedInUser = authInteractor.currentUserStateFlow

    private val _syncedSessionsLoaded = MutableStateFlow(true)
    val syncedSessionsLoaded = _syncedSessionsLoaded.asStateFlow()
    private val _localSessionsLoaded = MutableStateFlow(false)
    val localSessionsLoaded = _localSessionsLoaded.asStateFlow()

    private val clientUUID = settingsInteractor.appSettingsFlow.map { it.clientUUID ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _syncedSessions = MutableStateFlow(listOf<DomainSession>())
    val syncedSessions = combine(
        _syncedSessions,
        clientUUID
    ) { synced, clientUUID ->
        synced.map {
            it.toUiModel(
                currentClientUUID = clientUUID,
                isSynced = true
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _notOwnedSessionUUIDs = MutableStateFlow(listOf<String>())

    val isLoading = combine(
        localSessionsLoaded,
        syncedSessionsLoaded
    ) { localLoaded, syncedLoaded ->
        !localLoaded || !syncedLoaded
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // TODO: make a singular list for every session by combining
    // 1. synced
    // 2. local, now owned
    // 3. local, owned

    val notOwnedSessionUUIDs = _notOwnedSessionUUIDs.asStateFlow()
    val areThereSessionsNotOwned = notOwnedSessionUUIDs.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _ongoingSessionUUIDs = MutableStateFlow(listOf<String>())
    val ongoingSessionUUIDs = _ongoingSessionUUIDs.asStateFlow()

    val noSessionsToShow = combine(
        syncedSessions,
        ownedLocalSessionUUIDs,
        notOwnedSessionUUIDs,
        ongoingSessionUUIDs,
        isLoading
    ) { synced, owned, notOwned, ongoing, loading ->
        if (loading) {
            false
        } else {
            synced.isEmpty() && owned.isEmpty() && notOwned.isEmpty() && ongoing.isEmpty()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val canDeleteSessionsLocally = combine(
        ownedLocalSessionUUIDs,
        notOwnedSessionUUIDs,
        ongoingSessionUUIDs,
    ) { owned, notOwned, ongoing ->
        owned.size + notOwned.size - ongoing.size > 0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // TODO: make sessions sorted based on start time
    val allSessionUUIDs = combine(
        syncedSessions,
        ownedLocalSessionUUIDs,
        notOwnedSessionUUIDs,
    ) { synced, ownedLocal, notOwnedLocal ->
        val sessions = mutableListOf<String>()
        sessions.addAll(synced.map { it.uuid })
        sessions.addAll(ownedLocal)
        sessions.addAll(notOwnedLocal)
        val distinctSessions = sessions.distinct()
        distinctSessions.intersect(sessionStateFlows.keys).forEach { uuid ->
            val sessionFlow = sessionStateFlows[uuid]!!
            val isSynced = synced.any { it.uuid == uuid }
            if (sessionFlow.value?.isSynced != isSynced) {
                sessionFlow.value = sessionFlow.value?.copy(isSynced = isSynced)
            }
        }
        distinctSessions
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val canSyncSessions = combine(
        syncedSessions,
        ownedLocalSessionUUIDs,
        ongoingSessionUUIDs
    ) { synced, owned, ongoing ->
        // There is at least one session which can be synced (not ongoing).
        // The number of local sessions in the cloud is lower than local not ongoing sessions.
        synced.map { it.uuid }.intersect(owned.toSet()).size < owned.size - ongoing.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun loadLocalSessions() {
        _notOwnedSessionUUIDs.value = emptyList()
        _ongoingSessionUUIDs.value = emptyList()
        _ownedLocalSessionUUIDs.value = emptyList()
        _localSessionsLoaded.value = false

        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.getNotOwnedSessions().collectLatest { sessions ->
                _notOwnedSessionUUIDs.value = sessions.map { it.uuid }
                _localSessionsLoaded.value = true
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.getOngoingSessionUUIDs().collectLatest {
                _ongoingSessionUUIDs.value = it
                _localSessionsLoaded.value = true
            }
        }
        if (isUserSignedIn.value) {
            viewModelScope.launch(Dispatchers.IO) {
                sessionInteractor.getOwnSessions().collectLatest { sessions ->
                    _ownedLocalSessionUUIDs.value = sessions.map { it.uuid }
                    _localSessionsLoaded.value = true
                }
            }
        }
    }

    fun loadCloudSessions(activity: Activity) {
        _syncedSessions.value = emptyList()
        _syncedSessionsLoaded.value = false

        if (isUserSignedIn.value) {
            viewModelScope.launch(Dispatchers.IO) {
                sessionInteractor.loadSyncedSessions(activity)
                sessionInteractor.syncedSessions.collectLatest {
                    _syncedSessions.value = it ?: emptyList()
                    _syncedSessionsLoaded.value = it != null
                }
            }
        } else {
            _syncedSessionsLoaded.value = true
        }
    }

    fun deleteAllSyncedData() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.deleteAllSyncedData()
        }
    }

    fun ownSession(sessionUUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.ownSession(sessionUUID)
        }
    }

    fun ownAllSessions() {
        sessionInteractor.ownAllNotOwnedSessions()
    }

    fun syncSessions() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.uploadNotSyncedSessions()
        }
    }

    fun deleteSessionsLocally() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.deleteStoppedSessions()
            loadLocalSessions()
        }
    }

    fun disposeSessionStateFlow(sessionUUID: String) {
        sessionStateFlows.remove(sessionUUID)
    }

    fun getSessionStateFlow(sessionUUID: String): StateFlow<UiSession?> {
        if (sessionStateFlows.contains(sessionUUID)) {
            return sessionStateFlows[sessionUUID]!!.asStateFlow()
        }
        val sessionMutableStateFlow = MutableStateFlow<UiSession?>(null)
        sessionStateFlows[sessionUUID] = sessionMutableStateFlow

        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.getSession(sessionUUID).collectLatest { session ->
                if (session != null) {
                    sessionMutableStateFlow.value = session.toUiModel(
                        currentClientUUID = clientUUID.value,
                        isLocal = false,
                        isSynced = syncedSessions.value.any { it.uuid == sessionUUID }
                    )
                } else {
                    val remoteSession = syncedSessions.value.firstOrNull { it.uuid == sessionUUID }
                    sessionMutableStateFlow.value = remoteSession
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
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