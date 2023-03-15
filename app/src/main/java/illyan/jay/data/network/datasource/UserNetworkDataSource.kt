/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.network.datasource

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.toObject
import illyan.jay.data.network.model.FirestoreUser
import illyan.jay.domain.interactor.AuthInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val appLifecycle: Lifecycle,
) : DefaultLifecycleObserver {
    private val _userListenerRegistration = MutableStateFlow<ListenerRegistration?>(null)
    private val _userReference = MutableStateFlow<DocumentSnapshot?>(null)
    private val _user = MutableStateFlow<FirestoreUser?>(null)
    private val _cloudUser = MutableStateFlow<FirestoreUser?>(null)

    val user: StateFlow<FirestoreUser?> by lazy {
        if (_userListenerRegistration.value == null && !isLoading.value) {
            Timber.d("User StateFlow requested, but listener registration is null, reloading it")
            refreshUser()
        }
        _user.asStateFlow()
    }
    val cloudUser: StateFlow<FirestoreUser?> by lazy {
        if (_userListenerRegistration.value == null && !isLoadingFromCloud.value) {
            Timber.d("User StateFlow requested, but listener registration is null, reloading it")
            refreshUser()
        }
        _cloudUser.asStateFlow()
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isLoadingFromCloud = MutableStateFlow(false)
    val isLoadingFromCloud = _isLoadingFromCloud.asStateFlow()

    private val executor = Executors.newSingleThreadExecutor()

    init {
        authInteractor.addAuthStateListener {
            if (authInteractor.isUserSignedIn) {
                if (authInteractor.userUUID != null &&
                    authInteractor.userUUID != _user.value?.uuid
                ) {
                    Timber.d("Reloading snapshot listener for user ${_user.value?.uuid?.take(4)}")
                    refreshUser()
                } else {
                    Timber.d("User not changed from ${_user.value?.uuid?.take(4)}, not reloading snapshot listener on auth state change")
                }
            } else {
                Timber.d("Removing snapshot listener for user ${_user.value?.uuid?.take(4)}")
                resetUserListenerData()
            }
        }
        appLifecycle.addObserver(this)
    }

    private fun resetUserListenerData() {
        _userListenerRegistration.value?.remove()
        if (_userListenerRegistration.value != null) _userListenerRegistration.value = null
        if (_userReference.value != null) _userReference.value = null
        if (_isLoading.value) _isLoading.value = false
        if (_isLoadingFromCloud.value) _isLoadingFromCloud.value = false
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.d("Reload user on App Lifecycle Start")
        resetUserListenerData()
        refreshUser()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.d("Remove user listener on App Lifecycle Stop")
        resetUserListenerData()
    }

    private fun refreshUser(
        userUUID: String = authInteractor.userUUID.toString(),
        onError: (Exception) -> Unit = { Timber.e(it, "Error while getting user data: ${it.message}") },
        onSuccess: (FirestoreUser) -> Unit = {},
    ) {
        if (!authInteractor.isUserSignedIn || _isLoadingFromCloud.value) {
            Timber.d("Not refreshing user, due to another being loaded in or user is not signed in")
            return
        }
        resetUserListenerData()
        Timber.d("Connecting snapshot listener to Firebase to get ${userUUID.take(4)} user's data")
        val snapshotListener = EventListener<DocumentSnapshot> { snapshot, error ->
            Timber.v("New snapshot regarding user ${userUUID.take(4)}")
            if (error != null) {
                onError(error)
            } else {
                val user = snapshot?.toObject<FirestoreUser>()
                if (user == null) {
                    onError(NoSuchElementException("User document does not exist"))
                } else {
                    onSuccess(user)
                }
                if (snapshot != null) {
                    // Update _userReference value with snapshot when snapshot is not null
                    _userReference.value = snapshot
                } else if (_userReference.value != null) {
                    // If snapshot is null, then _userReference is invalid if not null. Assign null to it.
                    _userReference.value = null
                }

                // Cache
                if (user != null) {
                    if (_user.value != null) {
                        Timber.v("Refreshing Cached ${userUUID.take(4)} user's data")
                    } else {
                        Timber.d("Firestore loaded ${userUUID.take(4)} user's data from Cache")
                    }
                    _user.value = user
                } else if (_user.value != null) {

                    _user.value = null
                }
                if (_isLoading.value) {
                    _isLoading.value = false
                }

                // Cloud
                if (snapshot?.metadata?.isFromCache == false) {
                    if (user != null) {
                        if (_cloudUser.value != null) {
                            Timber.v("Firestore loaded fresh ${userUUID.take(4)} user's data from Cloud")
                        } else {
                            Timber.d("Firestore loaded ${userUUID.take(4)} user's data from Cloud")
                        }
                        _cloudUser.value = user
                    } else if (_cloudUser.value != null) {
                        _cloudUser.value = null
                    }
                }
                if (_isLoadingFromCloud.value && snapshot?.metadata?.isFromCache == false) {
                    _isLoadingFromCloud.value = false
                }
            }
        }
        _userListenerRegistration.value = firestore
            .collection(FirestoreUser.CollectionName)
            .document(userUUID)
            .addSnapshotListener(executor, MetadataChanges.INCLUDE, snapshotListener)
    }

    fun deleteUserData(
        onCancel: () -> Unit = { Timber.i("User data deletion canceled") },
        onFailure: (Exception) -> Unit = { Timber.e(it) },
        onSuccess: () -> Unit = { Timber.i("User data deletion successful") },
    ) {
        firestore.runBatch {
            deleteUserData(batch = it)
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    fun deleteUserData(
        batch: WriteBatch,
        onWriteFinished: () -> Unit = {}
    ) {
        _userReference.value?.apply {
            batch.delete(reference)
            onWriteFinished()
        }
    }
}
