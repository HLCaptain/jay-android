package illyan.jay.domain.interactor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import illyan.jay.service.BaseService
import illyan.jay.service.JayService
import illyan.jay.service.ServiceStateReceiver
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceInteractor @Inject constructor(
	private val serviceStateReceiver: ServiceStateReceiver,
	private val context: Context
) {

	init {
		LocalBroadcastManager.getInstance(context).registerReceiver(
			serviceStateReceiver,
			IntentFilter(BaseService.KEY_SERVICE_STATE_CHANGE)
		)
	}

	fun addServiceStateListener(listener: (isRunning: Boolean, name: String) -> Unit) {
		serviceStateReceiver.serviceStateListeners.add(listener)
	}

	fun removeServiceStateListener(listener: (isRunning: Boolean, name: String) -> Unit) {
		serviceStateReceiver.serviceStateListeners.remove(listener)
	}

	fun stopJayService() {
		if (isJayServiceRunning()) context.stopService(
			Intent(
				context,
				JayService::class.java
			)
		)
	}

	fun startJayService() {
		if (!isJayServiceRunning()) context.startForegroundService(
			Intent(
				context,
				JayService::class.java
			)
		)
	}

	// Ping pong example https://stackoverflow.com/a/39579191/16720445
	// Promising example: https://stackoverflow.com/questions/66742265/the-correct-way-to-determine-if-service-is-running
	// Commenting this out until I have a universal solution
//    fun<MyService : BaseService> isServiceRunning(serviceClass: KClass<MyService>): Boolean {
//        return false
//    }

	fun isJayServiceRunning() = JayService.isRunning
}
