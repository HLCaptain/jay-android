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
	fun getLocations(sessionId: Int) = locationInteractor.getLocations(sessionId)
		.flowOn(Dispatchers.IO)
		.map { it.map(DomainLocation::toUiModel) }
}

private fun DomainLocation.toUiModel() = UiLocation(
	id = id,
	latLng = latLng,
	sessionId = sessionId,
	time = time,
	color = Color.valueOf(Color.CYAN)
)