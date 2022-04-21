/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber
import javax.inject.Inject

/**
 * Service state receiver listens to service state changes
 * if registered as a Broadcast Receiver.
 * On message received, it notifies each listener which
 * subscribed to the state of each service.
 *
 * @constructor Create empty Service state receiver
 */
class ServiceStateReceiver @Inject constructor(

) : BroadcastReceiver() {

	val serviceStateListeners = mutableListOf<(Boolean, String) -> Unit>()

	override fun onReceive(context: Context?, intent: Intent?) {
		intent?.let {
			when(it.action) {
				// When the service changed states
				BaseService.KEY_SERVICE_STATE_CHANGE -> {
					val name = it.getStringExtra(BaseService.KEY_SERVICE_NAME).toString()
					val state = it.getStringExtra(BaseService.KEY_SERVICE_STATE_CHANGE)
					Timber.i("Service $name state = $state")
					when(state) {
						BaseService.SERVICE_RUNNING -> {
							serviceStateListeners.forEach { listener -> listener.invoke(true, name) }
						}
						BaseService.SERVICE_STOPPED -> {
							serviceStateListeners.forEach { listener -> listener.invoke(false, name) }
						}
					}
				}
			}
		}
	}
}