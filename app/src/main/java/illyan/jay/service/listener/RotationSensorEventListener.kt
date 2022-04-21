/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.service.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import illyan.jay.data.disk.toDomainRotation
import illyan.jay.domain.interactor.RotationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainRotation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Rotation sensor event listener.
 * On registration, it becomes active and saves data
 * via RotationInteractor.
 *
 * @property rotationInteractor saves data onto this interactor.
 * @param sessionInteractor using the session interactor to properly save
 * rotation sensor data for each individual session.
 * @constructor Create empty Rotation event listener
 */
class RotationSensorEventListener @Inject constructor(
	private val rotationInteractor: RotationInteractor,
	sessionInteractor: SessionInteractor
) : SessionSensorEventListener(sessionInteractor) {

	override fun onSensorChanged(event: SensorEvent?) {
		event?.let {
			val rotations = mutableListOf<DomainRotation>()
			ongoingSessionIds.forEach { sessionId ->
				rotations += it.toDomainRotation(sessionId)
			}
			scope.launch(Dispatchers.IO) { rotationInteractor.saveRotations(rotations) }
		}
	}

	override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

	}
}