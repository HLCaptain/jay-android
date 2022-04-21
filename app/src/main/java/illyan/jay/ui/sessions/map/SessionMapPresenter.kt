/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.sessions.map

import android.graphics.Color
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.ui.sessions.map.model.UiLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionMapPresenter @Inject constructor(
	private val locationInteractor: LocationInteractor
) {
	fun getLocations(sessionId: Long) = locationInteractor.getLocations(sessionId)
		.flowOn(Dispatchers.IO)
		.map { it.map(DomainLocation::toUiModel) }
}

private fun DomainLocation.toUiModel() = UiLocation(
	id = id,
	latLng = latLng,
	sessionId = sessionId,
	time = time,
	color = Color.valueOf(Color.MAGENTA)
)