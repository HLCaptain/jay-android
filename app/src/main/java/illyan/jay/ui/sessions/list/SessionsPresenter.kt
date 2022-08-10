/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.sessions.list

import co.zsmb.rainbowcake.withIOContext
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import illyan.jay.ui.sessions.list.model.UiLocation
import illyan.jay.ui.sessions.list.model.UiSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionsPresenter @Inject constructor(
	private val sessionInteractor: SessionInteractor,
	private val locationInteractor: LocationInteractor
) {
	fun getSessions() = sessionInteractor.getSessions()
		.map { it.map(DomainSession::toUiModel) }
		.flowOn(Dispatchers.IO)

	fun getLocations(sessionId: Long) = locationInteractor.getLocations(sessionId)
		.map { it.map(DomainLocation::toUiModel) }
		.flowOn(Dispatchers.IO)

	suspend fun deleteStoppedSessions() = withIOContext { sessionInteractor.deleteStoppedSessions() }
}

private fun DomainLocation.toUiModel() = UiLocation(
	id = id,
	latLng = latLng,
	speed = speed,
	sessionId = sessionId,
	time = time,
	accuracy = accuracy,
	bearing = bearing,
	bearingAccuracy = bearingAccuracy,
	altitude = altitude,
	speedAccuracy = speedAccuracy,
	verticalAccuracy = verticalAccuracy
)

private fun DomainSession.toUiModel() = UiSession(
	id = id,
	startTime = startTime,
	endTime = endTime,
	distance = distance
)