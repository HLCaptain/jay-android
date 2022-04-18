package illyan.jay.ui.toggle.service

sealed class ServiceToggleViewState

object Initial : ServiceToggleViewState()

object Loading : ServiceToggleViewState()

object On : ServiceToggleViewState()

object Off : ServiceToggleViewState()