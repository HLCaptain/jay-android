package illyan.jay.service.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorEventListener

class SimpleSensorEventListener(
    private var onSensorChangedCallback: (event: SensorEvent?) -> Unit = { _: SensorEvent? -> },
    private val onAccuracyChangedCallback: (sensor: Sensor?, accuracy: Int) -> Unit = { _: Sensor?, _: Int -> }
) : SensorEventListener {

    constructor(
        sensorEventCallback: SensorEventCallback
    ) : this() {
        onSensorChangedCallback = sensorEventCallback::onSensorChanged
    }

    override fun onSensorChanged(event: SensorEvent?) {
        onSensorChangedCallback.invoke(event)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        onAccuracyChangedCallback.invoke(sensor, accuracy)
    }
}