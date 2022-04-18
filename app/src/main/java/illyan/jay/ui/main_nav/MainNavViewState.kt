package illyan.jay.ui.main_nav

sealed class MainNavViewState

object Initial : MainNavViewState()

object Loading : MainNavViewState()

object Ready : MainNavViewState()