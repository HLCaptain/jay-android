/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.freedrive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.ServiceInteractor
import javax.inject.Inject

@HiltViewModel
class FreeDriveViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val serviceInteractor: ServiceInteractor
): ViewModel() {

    val isJayServiceRunning = serviceInteractor.isJayServiceRunning

    // FIXME: savedStateHandle somehow does not save state
    val startServiceAutomatically = savedStateHandle.getStateFlow(AutoStartServiceKey, false)

    fun load() {
        // TODO: decide whether to start the service or not based on saved preferences
        if (startServiceAutomatically.value) {
            serviceInteractor.startJayService()
        }
    }

    fun toggleService() {
        if (isJayServiceRunning.value) {
            serviceInteractor.stopJayService()
        } else {
            serviceInteractor.startJayService()
        }
    }

    fun setAutoStartService(startServiceAutomatically: Boolean) {
        savedStateHandle[AutoStartServiceKey] = startServiceAutomatically
    }

    companion object {
        const val AutoStartServiceKey = "AUTO_START_SERVICE"
    }
}