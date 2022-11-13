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
import kotlinx.coroutines.flow.first
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

    private val _sessionUUIDs = MutableStateFlow(listOf<String>())
    val localSessionUUIDs = _sessionUUIDs.asStateFlow()

    val isUserSignedIn = authInteractor.isUserSignedInStateFlow
    val signedInUser = authInteractor.currentUserStateFlow

    private val clientUUID = settingsInteractor.appSettingsFlow.map { it.clientUUID ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _syncedSessions = MutableStateFlow(listOf<DomainSession>())
    val syncedSessions = _syncedSessions.combine(clientUUID) { sessions, clientUUID -> sessions.map { it.toUiModel(currentClientUUID = clientUUID) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _syncedSessions.value.map { it.toUiModel(currentClientUUID = clientUUID.value) })

    fun loadLocalSessions() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.getLocalOnlySessionUUIDs().collectLatest {
                _sessionUUIDs.value = it.asReversed()
            }
        }
    }

    fun loadCloudSessions(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.syncSessions((context as Activity))
            sessionInteractor.syncedSessions.collectLatest {
                _syncedSessions.value = it ?: emptyList()
            }
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
        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.getSessions().first { sessions ->
                viewModelScope.launch(Dispatchers.IO) {
                    locationInteractor.getLocations(sessions.map { it.uuid }).first { locations ->
                        sessionInteractor.uploadSessions(
                            sessions,
                            locations,
                        )
                        true
                    }
                }
                true
            }
        }
    }

    fun deleteSessionsLocally() {
        sessionInteractor.deleteStoppedSessions()
    }

    fun getSessionStateFlow(sessionUUDI: String): StateFlow<UiSession?> {
        if (sessionStateFlows.contains(sessionUUDI)) {
            return sessionStateFlows[sessionUUDI]!!
        }
        val sessionMutableStateFlow = MutableStateFlow<UiSession?>(null)
        val sessionStateFlow = sessionMutableStateFlow.asStateFlow()
        sessionStateFlows[sessionUUDI] = sessionStateFlow

        viewModelScope.launch(Dispatchers.IO) {
            sessionInteractor.getSession(sessionUUDI).collectLatest { session ->
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
                    locationInteractor.getLocations(sessionUUDI).collectLatest {
                        sessionMutableStateFlow.value = session.toUiModel(it, clientUUID.value)
                    }
                }
            }
        }
        return sessionStateFlow
    }
}