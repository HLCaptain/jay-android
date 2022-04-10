package illyan.jay.ui.login

import illyan.jay.domain.interactor.AuthInteractor
import javax.inject.Inject

class LoginPresenter @Inject constructor(
    private val authInteractor: AuthInteractor
) {
    fun isUserLoggedIn(): Boolean = authInteractor.isUserLoggedIn()

    /**
     * Calls the listener when login status is changed.
     */
    fun addAuthStateListener(listener: (Boolean) -> Unit) {
        authInteractor.addAuthStateListener {
            listener(it.currentUser != null)
        }
    }
}