package illyan.jay.service.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import illyan.jay.data.disk.toDomainAcceleration
import illyan.jay.domain.interactor.AccelerationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AccelerationSensorEventListener @Inject constructor(
    private val accelerationInteractor: AccelerationInteractor,
    sessionInteractor: SessionInteractor
) : SessionSensorEventListener(sessionInteractor) {
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            ongoingSessionIds.forEach { sessionId ->
                scope.launch(Dispatchers.IO) {
                    accelerationInteractor.saveAcceleration(it.toDomainAcceleration(sessionId))
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}