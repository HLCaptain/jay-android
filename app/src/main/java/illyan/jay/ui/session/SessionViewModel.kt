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

package illyan.jay.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.maps.android.ktx.utils.sphericalPathLength
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.di.CoroutineDispatcherIO
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.ModelInteractor
import illyan.jay.domain.interactor.SensorEventInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainAggression
import illyan.jay.ui.session.model.UiLocation
import illyan.jay.ui.session.model.UiSession
import illyan.jay.ui.session.model.toUiModel
import illyan.jay.util.toZonedDateTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionInteractor: SessionInteractor,
    private val locationInteractor: LocationInteractor,
    private val sensorEventInteractor: SensorEventInteractor,
    private val modelInteractor: ModelInteractor,
    @CoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {

    private val _isModelAvailable = MutableStateFlow(false)
    val isModelAvailable = _isModelAvailable.asStateFlow()
    private val _aggressions = MutableStateFlow<Map<ZonedDateTime, Double>?>(null)
    val aggressions = _aggressions
        .asStateFlow()
        .map { aggressions -> aggressions?.map { it.value.toFloat() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val _path = MutableStateFlow<List<UiLocation>?>(null)
    val path = _path.combine(_aggressions) { path, aggressions ->
        path?.map { location ->
            // Find closest zonedDateTime of a location for each aggression
            val closestAggressionToLocationTimestamp = aggressions?.minByOrNull {
                // Time difference
                abs(it.key.toInstant().toEpochMilli() - location.zonedDateTime.toInstant().toEpochMilli())
            }?.value
//            Timber.d("Aggression difference: $closestAggressionToLocationTimestamp")
            location.copy(aggression = closestAggressionToLocationTimestamp?.toFloat())
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _path.value)

    private val _session = MutableStateFlow<UiSession?>(null)
    val session = combine(
        _session,
        _path
    ) { session, path ->
        session?.copy(
            totalDistance = path?.map { it.latLng }?.sphericalPathLength() ?: session.totalDistance
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _session.value)

    private val jobs = mutableListOf<Job>()

    init {
        viewModelScope.launch(dispatcherIO) {
            modelInteractor.downloadedModels.collect { models ->
                Timber.d("${models.size} models available")
                _isModelAvailable.update { models.isNotEmpty() }
            }
        }
    }

    fun load(sessionUUID: String) {
        jobs.forEach { it.cancel(CancellationException("Requested reload to data collection job, cancelling running jobs"))}
        jobs += viewModelScope.launch(dispatcherIO) {
            Timber.d("Trying to load session with ID: $sessionUUID")
            sessionInteractor.getSession(sessionUUID).collectLatest { session ->
                if (session != null) {
                    Timber.d("Loaded session with ID: $sessionUUID")
                    _session.update { session.toUiModel() }
                } else {
                    Timber.d("Session not found (collected null)")
                }
            }
        }
        jobs += viewModelScope.launch(dispatcherIO) {
            locationInteractor.getSyncedPath(sessionUUID).collectLatest { locations ->
                Timber.d("Loaded path with ${locations?.size} locations for session with ID: $sessionUUID")
                if (!locations.isNullOrEmpty()) {
                    val sortedPath = locations.sortedBy { it.zonedDateTime.toInstant() }
                    _path.update { sortedPath.map { it.toUiModel() } }
                }
            }
        }
        jobs += viewModelScope.launch(dispatcherIO) {
            locationInteractor.getSyncedPathAggressions(sessionUUID).collectLatest { aggressions ->
                Timber.d("Loaded ${aggressions?.size} aggressions for session with ID: $sessionUUID")
                _aggressions.update { aggressions?.associate {
                    Instant.ofEpochMilli(it.timestamp).toZonedDateTime() to it.aggression.toDouble()
                } }
            }
        }
    }

    fun generateAggressionByModel(sessionUUID: String) {
        viewModelScope.launch(dispatcherIO) {
            if (!aggressions.first().isNullOrEmpty()) {
                Timber.d("Aggressions already generated for session with ID ${sessionUUID.take(4)}, not generating new ones")
                return@launch
            }
            if (modelInteractor.downloadedModels.first().isEmpty()) {
                Timber.d("No downloaded models found")
                return@launch
            }
            Timber.d("Loading aggression for session with ID: ${sessionUUID.take(4)}")
            modelInteractor.downloadedModels.first().firstOrNull()?.let { model ->
                viewModelScope.launch(dispatcherIO) {
                    modelInteractor.getFilteredDriverAggression(
                        model.name,
                        sessionUUID
                    ).collectLatest { filteredAggressions ->
                        Timber.d("Loaded ${filteredAggressions.size} aggressions for session with ID: ${sessionUUID.take(4)}")
                        val aggressions = filteredAggressions.map {
                            DomainAggression(sessionUUID, it.key.toInstant().toEpochMilli(), it.value.toFloat())
                        }
                        locationInteractor.saveAggressions(aggressions)
                        sessionInteractor.syncedSessions.first()?.first { it.uuid == sessionUUID }?.let { session ->
                            sessionInteractor.uploadSessionAggressions(aggressions)
                        }
                        _aggressions.update { filteredAggressions }
                    }
                }
            }
        }
    }
}