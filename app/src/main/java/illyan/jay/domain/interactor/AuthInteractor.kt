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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.get
import illyan.jay.BuildConfig
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

    private val googleAuthStateListeners = mutableStateListOf<(Int) -> Unit>()

    val isUserSignedIn get() = auth.currentUser != null
    val userUUID get() = auth.currentUser?.uid

    private val _isSigningOut = MutableStateFlow(false)
    val isUserSigningOut = _isSigningOut.asStateFlow()

    init {
        refreshUserState(auth)
        addAuthStateListener(::refreshUserState)
    }

    private fun refreshUserState(state: FirebaseAuth = auth) {
        if (state.currentUser != null) {
            Timber.i("User ${state.currentUser!!.uid.take(4)} signed into Firebase")
        } else {
            Timber.i("User is signed out of Firebase")
        }
        _userStateFlow.update { state.currentUser }
        _isUserSignedInStateFlow.update { state.currentUser != null }
        _userPhotoUrlStateFlow.update { state.currentUser?.photoUrl }
        _userUUIDStateFlow.update { state.currentUser?.uid }
        _userDisplayNameStateFlow.update { state.currentUser?.displayName }
    }

    fun signOut() {
        Timber.i("Sign out requested for user ${userUUID?.take(4)}")
        _isSigningOut.update { true }
        val size = onSignOutListeners.size
        if (size == 0) {
            Timber.i("No sign out listeners detected, signing out user ${userUUID?.take(4)}")
            auth.signOut()
            _isSigningOut.update { false }
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
                auth.signOut()
                _isSigningOut.update { false }
            }
        }
    }

    fun signInViaGoogle(activity: MainActivity) {
        if (isUserSignedIn) return
        remoteConfig.fetchAndActivate().addOnSuccessListener {
            remoteConfig.ensureInitialized().addOnSuccessListener {
                val serverClientId = remoteConfig["default_web_client_id"].asString()
                if (serverClientId.isBlank()) {
                    Timber.e("Server client ID is blank in Remote Config")
                    googleAuthStateListeners.forEach { it(-1) }
                    googleAuthStateListeners.clear()
                    analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                        param(FirebaseAnalytics.Param.METHOD, "Google")
                    }
                } else {
                    coroutineScopeIO.launch {
                        try {
                            val credentialManager = CredentialManager.create(activity)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(serverClientId)
                                .setAutoSelectEnabled(true)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(activity, request)
                            val credential = result.credential
                            if (credential is CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val idToken = googleIdTokenCredential.idToken
                                signInWithCredential(
                                    activity,
                                    GoogleAuthProvider.getCredential(idToken, null)
                                )
                            } else {
                                Timber.e("Unexpected credential type: ${'$'}{credential.javaClass.name}")
                            }
                        } catch (e: GetCredentialException) {
                            Timber.e(e, "Credential retrieval failed")
                            googleAuthStateListeners.forEach { it(-1) }
                            googleAuthStateListeners.clear()
                            analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                                param(FirebaseAnalytics.Param.METHOD, "Google")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Credential retrieval error")
                            googleAuthStateListeners.forEach { it(-1) }
                            googleAuthStateListeners.clear()
                            analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                                param(FirebaseAnalytics.Param.METHOD, "Google")
                            }
                        }
                    }
                }
            }
        }
    }


    private fun signInWithCredential(
        activity: Activity,
        credential: AuthCredential
    ) {
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Timber.i("Firebase authentication successful")
            } else {
                // If sign in fails, display a message to the user.
                Timber.e(task.exception)
            }
        }
    }

    /**
     * Add authentication state listener
     *
     * @param listener listener to add to state changes.
     * @receiver receives a copy of current FirebaseAuth object as a state.
     */
    fun addAuthStateListener(listener: (FirebaseAuth) -> Unit) {
        auth.addAuthStateListener(listener)
    }

    // Each listener emit when they are ready to sign out
    private val onSignOutListeners = mutableListOf<(approveSignOut: () -> Unit) -> Unit>()

    /**
     * Sign Out listeners used to be called when the user is would like to sign out.
     * Each and every sign out listener should approve this sign out request by
     * calling the lambda method passed in as a parameter.
     */
    fun addOnSignOutListener(listener: (approveSignOut: () -> Unit) -> Unit) {
        onSignOutListeners.add(listener)
    }

    /**
     * Callback is only called once!
     */
    fun addGoogleAuthStateCallback(listener: (Int) -> Unit) {
        googleAuthStateListeners.add(listener)
    }
}

