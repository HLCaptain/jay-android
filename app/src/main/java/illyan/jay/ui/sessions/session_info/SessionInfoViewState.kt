package illyan.jay.ui.sessions.session_info

import illyan.jay.ui.sessions.session_info.model.UiSession

sealed class SessionInfoViewState

object Initial : SessionInfoViewState()

object Loading : SessionInfoViewState()

object NotFound : SessionInfoViewState()

data class Ready(val session: UiSession) : SessionInfoViewState()