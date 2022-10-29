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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.ui.sessions.model.UiSession
import illyan.jay.ui.sessions.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionInteractor: SessionInteractor,
    private val locationInteractor: LocationInteractor
): ViewModel() {
    private val sessionStateFlows = mutableMapOf<Long, StateFlow<UiSession?>>()

    private val _sessionIds = MutableStateFlow(listOf<Long>())
    val sessionIds = _sessionIds.asStateFlow()

    fun load() {
        viewModelScope.launch {
            sessionInteractor.getSessionIds().collectLatest {
                _sessionIds.value = it.sortedDescending()
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
                    locationInteractor.getLocations(sessionId).collectLatest {
                        sessionMutableStateFlow.value = session.toUiModel(it)
                    }
                }
            }
        }
        return sessionStateFlow
    }
}