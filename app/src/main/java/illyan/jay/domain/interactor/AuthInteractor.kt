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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import illyan.jay.MainActivity
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.util.awaitOperations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val analytics: FirebaseAnalytics,
    private val remoteConfig: FirebaseRemoteConfig,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
) {
    private val _userStateFlow = MutableStateFlow(auth.currentUser)
    val userStateFlow = _userStateFlow.asStateFlow()

    private val _isUserSignedInStateFlow = MutableStateFlow(auth.currentUser != null)
    val isUserSignedInStateFlow = _isUserSignedInStateFlow.asStateFlow()

    private val _userPhotoUrlStateFlow = MutableStateFlow(auth.currentUser?.photoUrl)
    val userPhotoUrlStateFlow = _userPhotoUrlStateFlow.asStateFlow()

    private val _userUUIDStateFlow = MutableStateFlow(auth.currentUser?.uid)
    val userUUIDStateFlow = _userUUIDStateFlow.asStateFlow()

    private val _userDisplayNameStateFlow = MutableStateFlow(auth.currentUser?.displayName)
    val userDisplayNameStateFlow = _userDisplayNameStateFlow.asStateFlow()

    private val _googleSignInClient = MutableStateFlow<GoogleSignInClient?>(null)
    private val googleSignInClient = _googleSignInClient.asStateFlow()

    private val googleAuthStateListeners = mutableStateListOf<(Int) -> Unit>()

    val isUserSignedIn get() = isUserSignedInStateFlow.value
    val userUUID get() = auth.currentUser?.uid

    private val _isSigningOut = MutableStateFlow(false)
    val isUserSigningOut = _isSigningOut.asStateFlow()

    init {
        addAuthStateListener { state ->
            if (state.currentUser != null) {
                Timber.i("User ${state.currentUser!!.uid.take(4)} signed into Firebase")
            } else {
                Timber.i("User ${userUUID?.take(4)} signed out of Firebase")
            }
            _userStateFlow.update { state.currentUser }
            _isUserSignedInStateFlow.update { state.currentUser != null }
            _userPhotoUrlStateFlow.update { state.currentUser?.photoUrl }
            _userUUIDStateFlow.update { state.currentUser?.uid }
            _userDisplayNameStateFlow.update { state.currentUser?.displayName }
        }
    }

    fun signOut() {
        Timber.i("Sign out requested for user ${userUUID?.take(4)}")
        _isSigningOut.update { true }
        val size = onSignOutListeners.size
        if (size == 0) {
            Timber.i("No sign out listeners detected, signing out user ${userUUID?.take(4)}")
        } else {
            Timber.i("Notifying sign out listeners")
            coroutineScopeIO.launch {
                awaitOperations(size) { onOperationFinished ->
                    onSignOutListeners.forEach {
                        coroutineScopeIO.launch {
                            it(onOperationFinished)
                        }
                    }
                }
                Timber.i("All listeners notified, signing out user ${userUUID?.take(4)}")
            }
            auth.signOut()
            googleSignInClient.value?.signOut()
            _isSigningOut.update { false }
        }
    }

    fun signInViaGoogle(activity: MainActivity) {
        if (isUserSignedIn) return
        if (_googleSignInClient.value == null) {
            remoteConfig.ensureInitialized().addOnCompleteListener {
                _googleSignInClient.value = GoogleSignIn.getClient(
                    activity,
                    GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(remoteConfig["default_web_client_id"].asString())
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
                        remoteConfig["default_web_client_id"].asString()
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
    private val onSignOutListeners = mutableListOf<(onOperationFinished: () -> Unit) -> Unit>()

    fun addOnSignOutListener(
        listener: (() -> Unit) -> Unit
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

