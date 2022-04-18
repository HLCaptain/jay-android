package illyan.jay.service.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import co.zsmb.rainbowcake.withIOContext
import illyan.jay.domain.interactor.SessionInteractor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn

abstract class SessionSensorEventListener(
	private val sessionInteractor: SessionInteractor
) : SensorEventListener {

	protected val scope = CoroutineScope(Dispatchers.IO)
	protected val ongoingSessionIds = mutableListOf<Int>()

	init {
		scope.launch {
			loadOngoingSessionIds()
		}
	}

	private suspend fun loadOngoingSessionIds() = withIOContext {
		sessionInteractor.getOngoingSessionIds()
			.flowOn(Dispatchers.IO)
			.collect {
				ongoingSessionIds.clear()
				ongoingSessionIds.addAll(it)
			}
	}

	override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
	override fun onSensorChanged(event: SensorEvent?) {}
}