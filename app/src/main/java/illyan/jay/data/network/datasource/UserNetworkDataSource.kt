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
import com.google.firebase.firestore.ktx.toObject
import illyan.jay.data.network.model.FirestoreUser
import illyan.jay.data.network.model.FirestoreUserWithUUID
import illyan.jay.domain.interactor.AuthInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

class UserNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val appLifecycle: Lifecycle,
) : DefaultLifecycleObserver {
    private val _userListenerRegistration = MutableStateFlow<ListenerRegistration?>(null)
    private val _userReference = MutableStateFlow<DocumentSnapshot?>(null)
    private val _user = MutableStateFlow<FirestoreUserWithUUID?>(null)
    val user: StateFlow<FirestoreUserWithUUID?> get() {
        if (_userListenerRegistration.value == null) loadUser()
        return _user.asStateFlow()
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val executor = Executors.newSingleThreadExecutor()

    init {
        authInteractor.addAuthStateListener {
            if (authInteractor.isUserSignedIn) {
                loadUser()
            } else {
                _userReference.value = null
                Timber.d("Removing snapshot listener for user ${_user.value?.uuid}")
                _userListenerRegistration.value?.remove()
                _user.value = null
            }
        }
        appLifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        _userListenerRegistration.value?.remove()
        _userListenerRegistration.value = null
    }

    private fun loadUser(
        userUUID: String = authInteractor.userUUID.toString(),
        onError: (Exception) -> Unit = { Timber.e(it, "Error while getting user data: ${it.message}") },
        onSuccess: (FirestoreUser) -> Unit = {},
    ) {
        if (_user.value == null) _isLoading.value = true
        Timber.d("Connecting snapshot listener to Firebase to get ${userUUID.take(4)} user's data")
        val snapshotListener = EventListener<DocumentSnapshot> { snapshot, error ->
            if (error != null) {
                onError(error)
            } else {
                val user = snapshot?.toObject<FirestoreUser>()
                if (user == null) {
                    onError(NoSuchElementException("User document does not exist"))
                } else {
                    Timber.d("Firebase loaded ${userUUID.take(4)} user's data")
                    onSuccess(user)
                }
                _userReference.value = snapshot
                if (user != null) {
                    _user.value = FirestoreUserWithUUID(userUUID, user)
                } else if (_user.value != null) {
                    _user.value = null
                }
                if (_isLoading.value) _isLoading.value = false
            }
        }
        _userListenerRegistration.value?.remove()
        _userListenerRegistration.value = firestore
            .collection(FirestoreUser.CollectionName)
            .document(userUUID)
            .addSnapshotListener(executor, snapshotListener)
    }

    fun deleteUserData(
        onCancel: () -> Unit = { Timber.d("User data deletion canceled") },
        onFailure: (Exception) -> Unit = { Timber.e(it) },
        onSuccess: () -> Unit = { Timber.d("User data deletion successful") },
    ) {
        _userReference.value?.apply {
            reference.delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
                .addOnCanceledListener { onCancel() }
        }
    }
}
