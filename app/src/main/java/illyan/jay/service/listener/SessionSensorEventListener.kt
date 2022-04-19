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
import android.hardware.SensorEventListener
import co.zsmb.rainbowcake.withIOContext
import illyan.jay.domain.interactor.SessionInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

abstract class SessionSensorEventListener(
	private val sessionInteractor: SessionInteractor
) : SensorEventListener {

	protected val scope = CoroutineScope(Dispatchers.IO)
	private val _ongoingSessionIds = mutableListOf<Long>()

	// Needed to guarantee safety from ConcurrentModificationException
	protected val ongoingSessionIds get() = _ongoingSessionIds.toList()

	init {
		scope.launch {
			loadOngoingSessionIds()
		}
	}

	private suspend fun loadOngoingSessionIds() = withIOContext {
		sessionInteractor.getOngoingSessionIds()
			.flowOn(Dispatchers.IO)
			.collect {
				_ongoingSessionIds.clear()
				_ongoingSessionIds.addAll(it)
			}
	}

	override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
	override fun onSensorChanged(event: SensorEvent?) {}
}