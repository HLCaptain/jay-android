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

package illyan.jay.service.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import illyan.jay.domain.interactor.SessionInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Session sensor event listener
 *
 * @property sessionInteractor
 * @constructor Create empty Session sensor event listener
 */
abstract class SessionSensorEventListener(
    private val sessionInteractor: SessionInteractor,
    coroutineScopeIO: CoroutineScope
) : SensorEventListener {
    /**
     * IDs of ongoing sessions. Needed to be private to ensure safety
     * from ConcurrentModificationExceptions.
     */
    private val _ongoingSessionIds = mutableListOf<String>()

    /**
     * Needed to guarantee safety from ConcurrentModificationExceptions.
     */
    protected val ongoingSessionUUIDs get() = _ongoingSessionIds.toList()

    init {
        coroutineScopeIO.launch {
            loadOngoingSessionIds()
        }
    }

    /**
     * Load ongoing session IDs with IO context on a non UI thread.
     */
    private suspend fun loadOngoingSessionIds() {
        sessionInteractor.getOngoingSessionUUIDs().collectLatest {
            _ongoingSessionIds.clear()
            _ongoingSessionIds.addAll(it)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Saved data does not change when accuracy is changed.
        // Accuracy data is provided by the SensorEvent
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // This method will probably be implemented in descendant classes.
    }
}
