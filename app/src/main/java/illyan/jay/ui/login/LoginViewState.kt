package illyan.jay.ui.login

sealed class LoginViewState

object Loading : LoginViewState()

data class LoginReady(val isLoggedIn: Boolean = false) : LoginViewState()