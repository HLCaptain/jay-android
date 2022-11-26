/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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
import android.content.Context
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
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {
    private val sessionStateFlows = mutableMapOf<String, StateFlow<UiSession?>>()

    private val _ownedLocalSessionUUIDs = MutableStateFlow(listOf<String>())
    val ownedLocalSessionUUIDs = _ownedLocalSessionUUIDs.asStateFlow()

    val isUserSignedIn = authInteractor.isUserSignedInStateFlow
    val signedInUser = authInteractor.currentUserStateFlow

    private val _syncedSessionsLoaded = MutableStateFlow(false)
    val syncedSessionsLoaded = _syncedSessionsLoaded.asStateFlow()
    private val _localSessionsLoaded = MutableStateFlow(false)
    val localSessionsLoaded = _localSessionsLoaded.asStateFlow()

    private val clientUUID = settingsInteractor.appSettingsFlow.map { it.clientUUID ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _syncedLocalSessionUUIDs = MutableStateFlow(listOf<String>())
    val syncedLocalSessionUUIDs = _syncedLocalSessionUUIDs.asStateFlow()

    private val _syncedSessions = MutableStateFlow(listOf<DomainSession>())
    val syncedSessions = combine(_syncedSessions, clientUUID, syncedLocalSessionUUIDs) { synced, clientUUID, locals -> synced.map { it.toUiModel(currentClientUUID = clientUUID, isLocal = locals.contains(it.uuid)) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _syncedSessions.value.map { it.toUiModel(currentClientUUID = clientUUID.value, isLocal = false) })

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
    ) { synced, owned, notOwned, ongoing, loading  ->
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
        syncedLocalSessionUUIDs
    ) { owned, notOwned, ongoing, syncedLocals ->
        if (syncedLocals.isNotEmpty()) {
            true
        } else {
            owned.size + notOwned.size - ongoing.size > 0
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val canSyncSessions = combine(
        ownedLocalSessionUUIDs,
        ongoingSessionUUIDs
    ) { ownedLocal, ongoingLocal ->
        // There is at least 1 session which is stopped and we own it, so it may be synced
        ownedLocal.size > ongoingLocal.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun loadLocalSessions() {
        _notOwnedSessionUUIDs.value = emptyList()
        _ongoingSessionUUIDs.value = emptyList()
        _ownedLocalSessionUUIDs.value = emptyList()
        _syncedLocalSessionUUIDs.value = emptyList()
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
            }
        }
        if (isUserSignedIn.value) {
            viewModelScope.launch(Dispatchers.IO) {
                sessionInteractor.getOwnLocalSessions()?.collectLatest { sessions ->
                    _ownedLocalSessionUUIDs.value = sessions.asReversed().map { it.uuid }
                    _localSessionsLoaded.value = true
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                sessionInteractor.getSyncedSessionsFromDisk().collectLatest { sessions ->
                    _syncedLocalSessionUUIDs.value = sessions.map { it.uuid }
                }
            }
        }
    }

    fun loadCloudSessions(context: Context) {
        _syncedSessions.value = emptyList()
        _syncedSessionsLoaded.value = false

        if (isUserSignedIn.value) {
            viewModelScope.launch(Dispatchers.IO) {
                sessionInteractor.loadSyncedSessions((context as Activity))
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
        sessionInteractor.ownSession(sessionUUID)
    }

    fun ownAllSessions() {
        sessionInteractor.ownAllNotOwnedSessions()
    }

    fun syncSessions() {
        sessionInteractor.uploadNotSyncedSessions()
    }

    fun deleteSessionsLocally() {
        sessionInteractor.deleteStoppedSessions()
    }

    fun getSessionStateFlow(sessionUUID: String): StateFlow<UiSession?> {
        if (sessionStateFlows.contains(sessionUUID)) {
            return sessionStateFlows[sessionUUID]!!
        }
        val sessionMutableStateFlow = MutableStateFlow<UiSession?>(null)
        val sessionStateFlow = sessionMutableStateFlow.asStateFlow()
        sessionStateFlows[sessionUUID] = sessionStateFlow

        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.getSession(sessionUUID).collectLatest { session ->
                session?.let {
                    if (session.startLocation == null ||
                        session.startLocationName == null
                    ) {
                        sessionInteractor.refreshSessionStartLocation(session)
                    }
                    if (session.endDateTime != null &&
                        (session.endLocation == null || session.endLocationName == null)
                    ) {
                        sessionInteractor.refreshSessionEndLocation(session)
                    }
                    locationInteractor.getLocations(sessionUUID).collectLatest {
                        sessionMutableStateFlow.value = session.toUiModel(it, clientUUID.value, true)
                    }
                }
            }
        }
        return sessionStateFlow
    }
}