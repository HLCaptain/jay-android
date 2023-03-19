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

package illyan.jay.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.di.CoroutineDispatcherIO
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.ui.session.model.GradientFilter
import illyan.jay.ui.session.model.UiLocation
import illyan.jay.ui.session.model.UiSession
import illyan.jay.ui.session.model.toUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionInteractor: SessionInteractor,
    private val locationInteractor: LocationInteractor,
    @CoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    private val _session = MutableStateFlow<UiSession?>(null)
    val session = _session.asStateFlow()

    private val _path = MutableStateFlow<List<UiLocation>?>(null)
    val path = _path.asStateFlow()

    private val _gradientFilter = MutableStateFlow(GradientFilter.Default)
    val gradientFilter = _gradientFilter.asStateFlow()

    fun load(sessionUUID: String) {
        viewModelScope.launch(dispatcherIO) {
            Timber.d("Trying to load session with ID: $sessionUUID")
            sessionInteractor.getSession(sessionUUID).collectLatest { session ->
                if (session != null) {
                    Timber.d("Loaded session with ID: $sessionUUID")
                    _session.value = session.toUiModel()
                    viewModelScope.launch(dispatcherIO) {
                        locationInteractor.getSyncedPath(sessionUUID).collectLatest { locations ->
                            Timber.d("Loaded path with ${locations?.size} locations for session with ID: $sessionUUID")
                            if (!locations.isNullOrEmpty()) {
                                val sortedPath = locations.sortedBy { it.zonedDateTime.toInstant() }
                                _path.value = sortedPath.map { it.toUiModel() }
                                _session.value = session.toUiModel(locations = sortedPath)
                            }
                        }
                    }
                } else {
                    Timber.d("Session not found (collected null)")
                }
            }
        }
    }

    fun setGradientFilter(filter: GradientFilter) {
        _gradientFilter.update { filter }
    }
}