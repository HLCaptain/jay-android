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
	suspend fun getSessions() = withIOContext {
		sessionInteractor.getSessions()
			.map { it.map(DomainSession::toUiModel) }
			.flowOn(Dispatchers.IO)
	}

	suspend fun getLocations(sessionId: Long) = withIOContext {
		locationInteractor.getLocations(sessionId)
			.map { it.map(DomainLocation::toUiModel) }
			.flowOn(Dispatchers.IO)
	}
}

private fun DomainLocation.toUiModel() = UiLocation(
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