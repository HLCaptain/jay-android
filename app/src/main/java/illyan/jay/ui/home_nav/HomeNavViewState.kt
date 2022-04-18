package illyan.jay.ui.home_nav

sealed class HomeNavViewState

object Initial : HomeNavViewState()
object Loading : HomeNavViewState()
object Ready : HomeNavViewState()