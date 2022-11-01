/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.domain.interactor

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import illyan.jay.MainActivity
import illyan.jay.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Auth interactor is an abstraction layer between higher level logic
 * and lower level implementation.
 * Based on Firebase's Authentication system.
 *
 * @constructor Create empty Auth interactor
 */
@Singleton
class AuthInteractor @Inject constructor(
    private val context: Context
) {
    private val _currentUserStateFlow = MutableStateFlow(Firebase.auth.currentUser)
    val currentUserStateFlow = _currentUserStateFlow.asStateFlow()

    private val _isUserSignedInStateFlow = MutableStateFlow(Firebase.auth.currentUser != null)
    val isUserSignedInStateFlow = _isUserSignedInStateFlow.asStateFlow()

    private val _userPhotoUrlStateFlow = MutableStateFlow(Firebase.auth.currentUser?.photoUrl)
    val userPhotoUrlStateFlow = _userPhotoUrlStateFlow.asStateFlow()

    private val _googleSignInClient = MutableStateFlow<GoogleSignInClient?>(null)
    private val googleSignInClient = _googleSignInClient.asStateFlow()

    private val googleAuthStateListeners = mutableStateListOf<(Int) -> Unit>()

    val isUserSignedIn get() = isUserSignedInStateFlow.value

    init {
        addAuthStateListener {
            _currentUserStateFlow.value = it.currentUser
            _isUserSignedInStateFlow.value = it.currentUser != null
            _userPhotoUrlStateFlow.value = it.currentUser?.photoUrl
        }
    }

    fun signOut() {
        Firebase.auth.signOut()
        googleSignInClient.value?.signOut()
    }

    fun signInViaGoogle(activity: MainActivity) {
        if (isUserSignedIn) return
        if (_googleSignInClient.value == null) {
            Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener {
                _googleSignInClient.value = GoogleSignIn.getClient(
                    activity,
                    GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(Firebase.remoteConfig["default_web_client_id"].asString())
                        .requestEmail()
                        .build()
                )
                activity.googleSignInLauncher.launch(googleSignInClient.value!!.signInIntent)
            }
        } else {
            activity.googleSignInLauncher.launch(googleSignInClient.value!!.signInIntent)
        }
    }

    fun handleGoogleSignInResult(
        activity: Activity,
        completedTask: Task<GoogleSignInAccount>
    ) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account.idToken?.let {
                signInWithCredential(
                    activity,
                    GoogleAuthProvider.getCredential(it, null)
                )
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            googleAuthStateListeners.forEach { it(e.statusCode) }
            googleAuthStateListeners.clear()
            Timber.e(
                e,
                "signInResult:failed code = ${e.statusCode}\n" +
                        "Used api key: " +
                        context.getString(R.string.default_web_client_id)
                            .substring(0, 4) + "..."
            )
        }
    }

    private fun signInWithCredential(
        activity: Activity,
        credential: AuthCredential
    ) {
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.i("signInWithCredential:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e(task.exception, "signInWithCredential:failure")
                }
            }
    }

    /**
     * Add authentication state listener
     *
     * @param listener listener to add to state changes.
     * @receiver receives a copy of current FirebaseAuth object as a state.
     */
    fun addAuthStateListener(
        listener: (FirebaseAuth) -> Unit
    ) {

        Firebase.auth.addAuthStateListener {
            listener(it)
        }
    }


    /**
     * Callback is only called once!
     */
    fun addGoogleAuthStateCallback(listener: (Int) -> Unit) {
        googleAuthStateListeners.add(listener)
    }
}

