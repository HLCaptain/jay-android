package illyan.jay.service

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.core.graphics.drawable.IconCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.R
import illyan.jay.domain.interactor.SensorInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.service.listener.LocationEventListener
import illyan.jay.service.listener.RotationSensorEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class JayService @Inject constructor(

) : BaseService() {

    @Inject lateinit var accelerationSensorEventListener: RotationSensorEventListener
    @Inject lateinit var rotationSensorEventListener: RotationSensorEventListener
    @Inject lateinit var locationEventListener: LocationEventListener
    @Inject lateinit var sensorInteractor: SensorInteractor
	@Inject lateinit var sessionInteractor: SessionInteractor
    lateinit var icon: IconCompat

	private var sessionId = -1L

    companion object {
        private const val NOTIFICATION_ID = 333
        const val CHANNEL_ID = "JayServiceChannel"
        var isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        icon = IconCompat.createWithResource(applicationContext, R.drawable.ic_launcher_foreground)
        startForeground(
            NOTIFICATION_ID,
            super.createNotification(
                "Jay service",
                "Starting location service...",
                CHANNEL_ID,
                NOTIFICATION_ID,
                icon
            )
        )

        scope.launch(Dispatchers.IO) {
            sessionId = sessionInteractor.startSession()
        }

        startSensors()
        return START_STICKY_COMPATIBILITY
    }

    private fun startSensors() {
        sensorInteractor.registerSensorListener(
            accelerationSensorEventListener,
            Sensor.TYPE_LINEAR_ACCELERATION,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorInteractor.registerSensorListener(
            rotationSensorEventListener,
            Sensor.TYPE_ROTATION_VECTOR,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        locationEventListener.locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                updateNotification(
                    "Current location",
                    "Lat: ${p0.lastLocation.latitude} Lng: ${p0.lastLocation.longitude}",
                    CHANNEL_ID,
                    NOTIFICATION_ID,
                    icon
                )
            }
        }
        sensorInteractor.requestLocationUpdates(
            locationEventListener.locationRequest,
            locationEventListener.locationCallback
        )
    }

    private fun stopSensors() {
        sensorInteractor.unregisterSensorListener(accelerationSensorEventListener)
        sensorInteractor.unregisterSensorListener(rotationSensorEventListener)
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

        // TODO: change int to long in ids, because it could cause problems with huge datasets
	    scope.launch(Dispatchers.IO) { sessionInteractor.stopOngoingSessions() }
        job.complete()
        isRunning = false
        super.onDestroy()
    }
}