package illyan.jay.ui.home

sealed class HomeViewState

object Initial : HomeViewState()

object Loading : HomeViewState()

object Ready : HomeViewState()