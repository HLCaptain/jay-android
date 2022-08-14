/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.toggle.service

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ServiceToggleViewModel @Inject constructor(
    private val serviceTogglePresenter: ServiceTogglePresenter
) : RainbowCakeViewModel<ServiceToggleViewState>(Initial) {

    fun load() = executeNonBlocking {
        viewState = Loading
        viewState = if (serviceTogglePresenter.isJayServiceRunning()) On else Off
        serviceTogglePresenter.addJayServiceStateListener { isRunning, _ ->
            viewState = if (isRunning) On else Off
        }
    }

    fun toggleService() = executeNonBlocking {
        viewState = Loading
        if (serviceTogglePresenter.isJayServiceRunning()) {
            serviceTogglePresenter.stopService()
        } else {
            serviceTogglePresenter.startService()
        }
    }
}
