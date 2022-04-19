/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.domain.interactor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import illyan.jay.service.BaseService
import illyan.jay.service.JayService
import illyan.jay.service.ServiceStateReceiver
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
