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

package illyan.jay.domain.interactor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import illyan.jay.service.BaseService
import illyan.jay.service.JayService
import illyan.jay.service.ServiceStateReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service interactor handles service interactions, such as
 * starting-stopping services and notifying receivers
 * on service state changes.
 *
 * @property serviceStateReceiver receives state updates
 * from services from the LocalBroadcastManager.
 * @property context starts and stops services.
 * @param localBroadcastManager needed to register the ServiceStateReceiver.
 * @constructor Create empty Service interactor
 */
@Singleton
class ServiceInteractor @Inject constructor(
    private val serviceStateReceiver: ServiceStateReceiver,
    localBroadcastManager: LocalBroadcastManager,
    private val context: Context
) {

    init {
        serviceStateReceiver.serviceStateListeners.add { isServiceRunning, serviceName ->
            if (serviceName == JayService::class.simpleName) {
                _isJayServiceRunning.value = isServiceRunning
            }
        }
        localBroadcastManager.registerReceiver(
            serviceStateReceiver,
            IntentFilter(BaseService.KEY_SERVICE_STATE_CHANGE)
        )
    }

    /**
     * Add service state listener.
     *
     * @param listener gets called when a service's status is changed.
     * @receiver receives the name of the service and that the service is running or not.
     */
    fun addServiceStateListener(listener: (isRunning: Boolean, name: String) -> Unit) {
        serviceStateReceiver.serviceStateListeners.add(listener)
    }

    /**
     * Remove service state listener.
     *
     * @param listener unsubscribing from state changes.
     * @receiver receives the name of the service and that the service is running or not.
     */
    fun removeServiceStateListener(listener: (isRunning: Boolean, name: String) -> Unit) {
        serviceStateReceiver.serviceStateListeners.remove(listener)
    }

    /**
     * Stop Jay service.
     */
    fun stopJayService() = if (isJayServiceRunning()) {
        context.stopService(Intent(context, JayService::class.java))
    } else false

    /**
     * Start Jay service in the Foreground.
     */
    fun startJayService() = if (!isJayServiceRunning()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, JayService::class.java))
        } else {
            context.startService(Intent(context, JayService::class.java))
        }
    } else null

    // Ping pong example https://stackoverflow.com/a/39579191/16720445
    // Promising example: https://stackoverflow.com/questions/66742265/the-correct-way-to-determine-if-service-is-running
    // Commenting this out until I have a universal solution
//    fun<MyService : BaseService> isServiceRunning(serviceClass: KClass<MyService>): Boolean {
//        return false
//    }

    /**
     * Gets whether Jay service is running or not.
     *
     * @return true if Jay is running in the foreground, otherwise false.
     */
    fun isJayServiceRunning() = JayService.isRunning
    private val _isJayServiceRunning = MutableStateFlow(JayService.isRunning)
    val isJayServiceRunning = _isJayServiceRunning.asStateFlow()
}
