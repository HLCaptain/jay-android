package illyan.jay.ui.sessions.map

import illyan.jay.domain.model.DomainLocation
import illyan.jay.ui.sessions.map.model.UiLocation

sealed class SessionMapViewState

object Initial : SessionMapViewState()
object Loading : SessionMapViewState()
data class Ready(val locations: List<UiLocation>, val firstLoaded: Boolean) : SessionMapViewState()