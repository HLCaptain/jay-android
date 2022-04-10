package illyan.jay

sealed class MainViewState

object MainStart : MainViewState()

data class MainReady(val isLoggedIn: Boolean = false) : MainViewState()
