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