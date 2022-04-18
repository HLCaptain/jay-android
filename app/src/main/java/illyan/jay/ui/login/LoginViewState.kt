package illyan.jay.ui.login

sealed class LoginViewState

object Initial : LoginViewState()

object Loading : LoginViewState()

object LoggingIn : LoginViewState()

object LoggedIn : LoginViewState()

object LoggedOut : LoginViewState()