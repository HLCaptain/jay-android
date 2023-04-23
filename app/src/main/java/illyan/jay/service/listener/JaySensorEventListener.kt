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

import android.hardware.SensorEvent
import illyan.jay.data.room.toDomainModel
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.SensorEventInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainSensorEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Acceleration sensor event listener.
 * On registration, it becomes active and saves data
 * via AccelerationInteractor.
 *
 * @property sensorEventInteractor saves data onto this interactor.
 * @param sessionInteractor using the session interactor to properly save
 * acceleration sensor data for each individual session.
 * @constructor Create empty Acceleration event listener
 */
class JaySensorEventListener @Inject constructor(
    private val sensorEventInteractor: SensorEventInteractor,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
    sessionInteractor: SessionInteractor,
) : SessionSensorEventListener(sessionInteractor, coroutineScopeIO) {

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val sensorEvents = mutableListOf<DomainSensorEvent>()
            ongoingSessionUUIDs.forEach { sessionUUID ->
                sensorEvents += it.toDomainModel(sessionUUID)
            }
            // Saving data for each session
            coroutineScopeIO.launch { sensorEventInteractor.saveSensorEvents(sensorEvents) }
        }
    }
}
