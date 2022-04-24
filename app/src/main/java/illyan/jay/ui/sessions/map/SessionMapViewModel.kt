/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.sessions.map

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionMapViewModel @Inject constructor(
	private val sessionMapPresenter: SessionMapPresenter
) : RainbowCakeViewModel<SessionMapViewState>(Initial) {

	fun loadPath(sessionId: Long) = executeNonBlocking {
		viewState = Loading
		var doAnimateCamera = true

		sessionMapPresenter.getLocations(sessionId).collect {
			if (it.isNotEmpty()) {
				viewState = Ready(it, doAnimateCamera)
				doAnimateCamera = false
			} else {
				doAnimateCamera = true
			}
		}
	}
}