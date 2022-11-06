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
import illyan.jay.domain.model.DomainSession
import illyan.jay.ui.sessions.model.UiSession
import illyan.jay.ui.sessions.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionInteractor: SessionInteractor,
    private val locationInteractor: LocationInteractor,
    private val authInteractor: AuthInteractor
) : ViewModel() {
    private val sessionStateFlows = mutableMapOf<Long, StateFlow<UiSession?>>()

    private val _sessionIds = MutableStateFlow(listOf<Long>())
    val sessionIds = _sessionIds.asStateFlow()

    val isUserSignedIn = authInteractor.isUserSignedInStateFlow

    private val _syncedSessions = MutableStateFlow(listOf<DomainSession>())
    val syncedSessions = _syncedSessions.map { sessions -> sessions.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _syncedSessions.value.map { it.toUiModel() })

    fun load(context: Context) {
        viewModelScope.launch {
            sessionInteractor.getLocalOnlySessionIds().collectLatest {
                _sessionIds.value = it.sortedDescending()
            }
        }
        viewModelScope.launch {
            sessionInteractor.getSyncedSessions(
                (context as Activity)
            ) {
                Timber.d("Got sessions from Firebase, size = ${it.size}")
                _syncedSessions.value = it
            }
        }
    }

    fun deleteAllSyncedData() {
        viewModelScope.launch {
            sessionInteractor.deleteAllSyncedData(viewModelScope)
        }
    }

    fun syncSessions() {
        viewModelScope.launch {
            sessionInteractor.getSessions().first { sessions ->
                sessions.forEach {
                    if (it.uuid == null) {
                        viewModelScope.launch {
                            locationInteractor.getLocations(it.id).first { locations ->
                                sessionInteractor.uploadSession(
                                    it to locations,
                                    viewModelScope
                                )
                                locations.isNotEmpty()
                            }
                        }
                    }
                }
                sessions.isNotEmpty()
            }
        }
    }

    fun getSessionStateFlow(sessionId: Long): StateFlow<UiSession?> {
        if (sessionStateFlows.contains(sessionId)) {
            return sessionStateFlows[sessionId]!!
        }
        val sessionMutableStateFlow = MutableStateFlow<UiSession?>(null)
        val sessionStateFlow = sessionMutableStateFlow.asStateFlow()
        sessionStateFlows[sessionId] = sessionStateFlow

        viewModelScope.launch {
            sessionInteractor.getSession(sessionId).collectLatest { session ->
                session?.let {
                    if (session.startLocation == null ||
                        session.startLocationName == null
                    ) {
                        sessionInteractor.refreshSessionStartLocation(
                            session,
                            viewModelScope
                        )
                    }
                    if (session.endDateTime != null &&
                        (session.endLocation == null || session.endLocationName == null)
                    ) {
                        sessionInteractor.refreshSessionEndLocation(
                            session,
                            viewModelScope
                        )
                    }
                    locationInteractor.getLocations(sessionId).collectLatest {
                        sessionMutableStateFlow.value = session.toUiModel(it)
                    }
                }
            }
        }
        return sessionStateFlow
    }
}