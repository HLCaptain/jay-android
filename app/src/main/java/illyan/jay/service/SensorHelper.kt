package illyan.jay.service

import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorHelper(
    private val sensorManager: SensorManager
) {
    companion object {
        const val SENSOR_CHANGED = "SENSOR_CHANGED"
        const val KEY_SENSOR = "KEY_SENSOR"
    }

    fun registerSensorListener(
        listener: SensorEventListener,
        type: Int,
        delay: Int
    ) {
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(type), delay)
    }

    fun updateSensorListener(
        newListener: SensorEventListener,
        oldListener: SensorEventListener,
        type: Int,
        delay: Int
    ) {
        unregisterSensorListener(oldListener)
        registerSensorListener(newListener, type, delay)
    }

    fun unregisterSensorListener(listener: SensorEventListener) {
        sensorManager.unregisterListener(listener)
    }
}