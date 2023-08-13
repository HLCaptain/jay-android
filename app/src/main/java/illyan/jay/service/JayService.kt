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

package illyan.jay.service

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.data.room.model.SensorOptions
import illyan.jay.domain.interactor.SensorInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.service.listener.JaySensorEventListener
import illyan.jay.service.listener.LocationEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Jay service used to collect data from all kinds of sensors.
 *
 * @constructor Create empty Jay service
 */
@AndroidEntryPoint
class JayService @Inject constructor() : BaseService() {

    @Inject
    lateinit var jaySensorEventListener: JaySensorEventListener

    @Inject
    lateinit var locationEventListener: LocationEventListener

    @Inject
    lateinit var sensorInteractor: SensorInteractor

    @Inject
    lateinit var sessionInteractor: SessionInteractor

    @Inject
    lateinit var icon: IconCompat

    private var sessionUUID = ""

    companion object {
        private const val NOTIFICATION_ID = 333
        const val CHANNEL_ID = "JayServiceChannel"
        var isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            super.createNotification(
                "Jay service",
                "Starting location service...",
                CHANNEL_ID,
                NOTIFICATION_ID,
                icon,
                "Collecting sensor information to analyze locally."
            )
        )

        // Starting sensors right after starting the sessions. Not necessary, but no harm.
        scope.launch {
            sessionUUID = sessionInteractor.startSession()
        }.invokeOnCompletion { startSensors() }

        return START_STICKY_COMPATIBILITY
    }

    private fun startSensors() {

        // 1. Getting present sensors on the device
        val sensorTypes = sensorInteractor.sensors.map { it.type }
        Timber.d("Sensors present on the device: ${
            sensorInteractor.sensors.joinToString { it.stringType }
        }")

        // 2. Select preferred sensors if found
        val accelerationSensorType = if (sensorTypes.contains(Sensor.TYPE_LINEAR_ACCELERATION)) {
            Sensor.TYPE_LINEAR_ACCELERATION to Sensor.STRING_TYPE_LINEAR_ACCELERATION
        } else {
            Sensor.TYPE_ACCELEROMETER to Sensor.STRING_TYPE_ACCELEROMETER
        }
        val rotationSensorType = if (sensorTypes.contains(Sensor.TYPE_ROTATION_VECTOR)) {
            Sensor.TYPE_ROTATION_VECTOR to Sensor.STRING_TYPE_ROTATION_VECTOR
        } else {
            Sensor.TYPE_ORIENTATION to Sensor.STRING_TYPE_ORIENTATION
        }
        val magneticFieldSensorType =
            Sensor.TYPE_MAGNETIC_FIELD to Sensor.STRING_TYPE_MAGNETIC_FIELD

        // 3. Register listeners
        // TODO: Use set delay from user preferences in the future, default is NORMAL
        listOf(
            SensorOptions(
                accelerationSensorType.first,
                accelerationSensorType.second,
                SensorManager.SENSOR_DELAY_NORMAL
            ),
            SensorOptions(
                rotationSensorType.first,
                rotationSensorType.second,
                SensorManager.SENSOR_DELAY_NORMAL
            ),
            SensorOptions(
                magneticFieldSensorType.first,
                magneticFieldSensorType.second,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        ).forEach {
            if (sensorTypes.contains(it.sensorType)) {
                Timber.d("Registering listener to sensor type: ${it.sensorTypeString}")
                sensorInteractor.registerSensorListener(
                    jaySensorEventListener,
                    it.sensorType,
                    it.sensorDelay
                )
            } else {
                Timber.d("Sensor ${it.sensorTypeString} is not present on the device")
            }
        }

        // Start location requests
        sensorInteractor.requestLocationUpdates(
            locationEventListener.locationCallback,
            locationEventListener.locationRequest
        )
    }

    private fun stopSensors() {
        sensorInteractor.unregisterSensorListener(jaySensorEventListener)
        sensorInteractor.removeLocationUpdates(locationEventListener.locationCallback)
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onDestroy() {
        stopSensors()
        removeNotification(NOTIFICATION_ID)
        scope.launch {
            sessionInteractor.stopOngoingSessions()
        }
        job.complete()
        isRunning = false
        super.onDestroy()
    }
}


