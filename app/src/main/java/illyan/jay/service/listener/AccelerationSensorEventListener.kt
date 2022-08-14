/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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
import illyan.jay.data.disk.toDomainAcceleration
import illyan.jay.domain.interactor.AccelerationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainAcceleration
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Acceleration sensor event listener.
 * On registration, it becomes active and saves data
 * via AccelerationInteractor.
 *
 * @property accelerationInteractor saves data onto this interactor.
 * @param sessionInteractor using the session interactor to properly save
 * acceleration sensor data for each individual session.
 * @constructor Create empty Acceleration event listener
 */
class AccelerationSensorEventListener @Inject constructor(
    private val accelerationInteractor: AccelerationInteractor,
    sessionInteractor: SessionInteractor
) : SessionSensorEventListener(sessionInteractor) {

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val accelerations = mutableListOf<DomainAcceleration>()
            ongoingSessionIds.forEach { sessionId ->
                accelerations += it.toDomainAcceleration(sessionId)
            }
            // Saving data for each session
            scope.launch { accelerationInteractor.saveAccelerations(accelerations) }
        }
    }
}
