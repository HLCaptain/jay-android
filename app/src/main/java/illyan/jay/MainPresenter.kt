package illyan.jay

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import illyan.jay.domain.interactor.AuthInteractor
import javax.inject.Inject

class MainPresenter @Inject constructor(
    private var authInteractor: AuthInteractor
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