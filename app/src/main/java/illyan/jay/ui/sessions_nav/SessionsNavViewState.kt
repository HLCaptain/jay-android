package illyan.jay.ui.sessions_nav

sealed class SessionsNavViewState

object Initial : SessionsNavViewState()

object Loading : SessionsNavViewState()

object Ready : SessionsNavViewState()