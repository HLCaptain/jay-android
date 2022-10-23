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

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.ServiceInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class FreeDriveViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val serviceInteractor: ServiceInteractor
): ViewModel() {

    val isJayServiceRunning = serviceInteractor.isJayServiceRunning

    // FIXME: savedStateHandle somehow does not save state
    val startServiceAutomatically = settingsInteractor.appSettingsFlow.map { it.turnOnFreeDriveAutomatically }

    suspend fun load() {
        // TODO: decide whether to start the service or not based on saved preferences
        startServiceAutomatically.first {
            if (it) serviceInteractor.startJayService()
            true
        }
    }

    fun toggleService() {
        if (isJayServiceRunning.value) {
            serviceInteractor.stopJayService()
        } else {
            serviceInteractor.startJayService()
        }
    }

    suspend fun setAutoStartService(startServiceAutomatically: Boolean) {
        settingsInteractor.updateAppSettings {
            it.copy(turnOnFreeDriveAutomatically = startServiceAutomatically)
        }
    }
}