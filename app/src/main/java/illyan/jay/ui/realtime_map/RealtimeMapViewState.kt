package illyan.jay.ui.realtime_map

import illyan.jay.ui.realtime_map.model.UiLocation

sealed class RealtimeMapViewState

object Initial : RealtimeMapViewState()

object Loading : RealtimeMapViewState()

data class Ready(val currentLocation: UiLocation) : RealtimeMapViewState()