package illyan.jay.domain.interactor

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject


class AuthInteractor @Inject constructor() {
    fun isUserLoggedIn(): Boolean = Firebase.auth.currentUser != null

    fun addAuthStateListener(listener: (FirebaseAuth) -> Unit) {
        Firebase.auth.addAuthStateListener {
            listener(it)
        }
    }
}