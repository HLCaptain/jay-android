package illyan.jay.service.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import illyan.jay.data.disk.toDomainRotation
import illyan.jay.domain.interactor.RotationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class RotationSensorEventListener @Inject constructor(
    private val rotationInteractor: RotationInteractor,
    sessionInteractor: SessionInteractor
) : SessionSensorEventListener(sessionInteractor) {

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            ongoingSessionIds.toList().forEach { sessionId ->
                scope.launch(Dispatchers.IO) {
                    rotationInteractor.saveRotation(it.toDomainRotation(sessionId))
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}