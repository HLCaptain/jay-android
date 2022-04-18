package illyan.jay.ui.realtime_map_nav

sealed class RealtimeMapNavViewState

object Initial : RealtimeMapNavViewState()
object Loading : RealtimeMapNavViewState()
object Ready : RealtimeMapNavViewState()