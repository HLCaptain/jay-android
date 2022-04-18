package illyan.jay.ui.sessions.list

import illyan.jay.ui.sessions.list.model.UiSession

sealed class SessionsViewState

object Initial : SessionsViewState()

object Loading : SessionsViewState()

data class Ready(val sessions: List<UiSession> = listOf()) : SessionsViewState()