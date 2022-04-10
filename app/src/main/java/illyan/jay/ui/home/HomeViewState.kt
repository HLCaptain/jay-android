package illyan.jay.ui.home

sealed class HomeViewState

object Loading : HomeViewState()

object HomeReady: HomeViewState()