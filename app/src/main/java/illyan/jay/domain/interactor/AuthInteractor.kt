/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import illyan.jay.MainActivity
import illyan.jay.R
import illyan.jay.di.CoroutineScopeIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val auth: FirebaseAuth,
    private val context: Context,
    private val analytics: FirebaseAnalytics,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
) {
    private val _currentUserStateFlow = MutableStateFlow(auth.currentUser)
    val currentUserStateFlow = _currentUserStateFlow.asStateFlow()

    private val _isUserSignedInStateFlow = MutableStateFlow(auth.currentUser != null)
    val isUserSignedInStateFlow = _isUserSignedInStateFlow.asStateFlow()

    private val _userPhotoUrlStateFlow = MutableStateFlow(auth.currentUser?.photoUrl)
    val userPhotoUrlStateFlow = _userPhotoUrlStateFlow.asStateFlow()

    private val _googleSignInClient = MutableStateFlow<GoogleSignInClient?>(null)
    private val googleSignInClient = _googleSignInClient.asStateFlow()

    private val googleAuthStateListeners = mutableStateListOf<(Int) -> Unit>()

    val isUserSignedIn get() = isUserSignedInStateFlow.value
    val userUUID get() = currentUserStateFlow.value?.uid

    private val _isSigningOut = MutableStateFlow(false)
    val isUserSigningOut = _isSigningOut.asStateFlow()

    init {
        addAuthStateListener {
            if (it.currentUser != null) {
                Timber.i("User ${it.currentUser!!.uid.take(4)} signed into Firebase")
            } else {
                Timber.i("User ${userUUID?.take(4)} signed out of Firebase")
            }
            _currentUserStateFlow.value = it.currentUser
            _isUserSignedInStateFlow.value = it.currentUser != null
            _userPhotoUrlStateFlow.value = it.currentUser?.photoUrl
        }
    }

    fun signOut() {
        Timber.i("Sign out requested for user ${userUUID?.take(4)}")
        _isSigningOut.value = true
        val size = onSignOutListeners.size
        if (size == 0) {
            Timber.i("No sign out listeners detected, signing out user ${userUUID?.take(4)}")
            auth.signOut()
            googleSignInClient.value?.signOut()
            _isSigningOut.value = false
        } else {
            Timber.i("Notifying sign out listeners")
            val approvedListeners = MutableStateFlow(0)
            onSignOutListeners.forEach {
                coroutineScopeIO.launch {
                    it(auth).first {
                        approvedListeners.value++
                        Timber.d("${approvedListeners.value++} listeners approved sign out")
                        true
                    }
                }
            }
            coroutineScopeIO.launch {
                approvedListeners.first {
                    if (it >= size) {
                        Timber.i("All listeners notified, signing out user ${userUUID?.take(4)}")
                        auth.signOut()
                        googleSignInClient.value?.signOut()
                        _isSigningOut.value = false
                    }
                    it >= size
                }
            }
        }
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
                            .take(4) + "..." +
                        "\n${e.message}"
            )
            analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                param(FirebaseAnalytics.Param.METHOD, "Google")
            }
        }
    }

    private fun signInWithCredential(
        activity: Activity,
        credential: AuthCredential
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.i("Firebase authentication successful")
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e(task.exception, task.exception?.message)
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
        auth.addAuthStateListener {
            listener(it)
        }
    }

    // Each listener emit when they are ready to sign out
    private val onSignOutListeners = mutableListOf<(FirebaseAuth) -> Flow<Unit>>()

    fun addOnSignOutListener(
        listener: (FirebaseAuth) -> Flow<Unit>
    ) {
        onSignOutListeners.add(listener)
    }

    /**
     * Callback is only called once!
     */
    fun addGoogleAuthStateCallback(listener: (Int) -> Unit) {
        googleAuthStateListeners.add(listener)
    }
}

