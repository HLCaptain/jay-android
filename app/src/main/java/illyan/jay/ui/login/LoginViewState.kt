package illyan.jay.ui.login

sealed class LoginViewState

object Loading : LoginViewState()

object LoggingIn : LoginViewState()

data class LoginReady(val isLoggedIn: Boolean = false) : LoginViewState()