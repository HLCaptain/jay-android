package illyan.jay

sealed class MainViewState

object Initial : MainViewState()

object LoggedIn : MainViewState()

object LoggedOut : MainViewState()
