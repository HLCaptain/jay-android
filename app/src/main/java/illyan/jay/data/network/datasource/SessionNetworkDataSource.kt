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

package illyan.jay.data.network.datasource

import android.app.Activity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.maps.android.ktx.utils.sphericalPathLength
import illyan.jay.data.network.model.PathDocument
import illyan.jay.data.network.toDomainSession
import illyan.jay.data.network.toHashMap
import illyan.jay.data.network.toPaths
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
) {
    fun getSessions(
        activity: Activity? = null,
        userUUID: String = authInteractor.userUUID.toString(),
        listener: (List<DomainSession>?) -> Unit,
    ): ListenerRegistration? {
        return if (authInteractor.isUserSignedIn) {
            Timber.d("Connecting snapshot listener to Firebase")
            val snapshotListener = EventListener<DocumentSnapshot> { snapshot, error ->
                val domainSessions = (snapshot?.get("sessions") as List<Map<String, Any>>?)?.map {
                    it.toDomainSession(it["uuid"] as String, authInteractor.userUUID!!)
                } ?: emptyList()
                Timber.d("Firebase got sessions with IDs: ${
                    domainSessions.map { it.uuid.substring(0..3) }
                }")
                listener(domainSessions)
            }
            firestore
                .collection(UsersCollectionPath)
                .document(userUUID)
                .run { if (activity != null)
                    addSnapshotListener(activity, snapshotListener)
                else
                    addSnapshotListener(snapshotListener)
                }
        } else {
            Timber.d("Connecting snapshot listener to Firebase")
            listener(null)
            null
        }
    }

    fun insertSession(
        domainSession: DomainSession,
        domainLocations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit
    ) = insertSessions(listOf(domainSession), domainLocations, onSuccess)

    fun insertSessions(
        domainSessions: List<DomainSession>,
        domainLocations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit
    ) {
        if (!authInteractor.isUserSignedIn) return
        val paths = mutableListOf<PathDocument>()
        domainSessions.forEach { session ->
            val locationsForThisSession = domainLocations.filter { it.sessionUUID.contentEquals(session.uuid) }
            if (session.distance == null) {
                session.distance = locationsForThisSession
                    .sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
                    .map { it.latLng }.sphericalPathLength().toFloat()
            }
            paths.addAll(locationsForThisSession.toPaths(session.uuid))
        }
        val pathRefs = paths.map {
            firestore
                .collection(PathsCollectionPath)
                .document(it.uuid) to it
        }
        val userRef = firestore
            .collection(UsersCollectionPath)
            .document(authInteractor.userUUID!!)

        firestore.runBatch { batch ->
            pathRefs.forEach { batch.set(it.first, it.second.toHashMap()) }

            batch.set(
                userRef,
                mapOf("sessions" to FieldValue.arrayUnion(*domainSessions.map { it.toHashMap() }.toTypedArray())),
                SetOptions.merge()
            )
        }.addOnSuccessListener {
            onSuccess(domainSessions)
        }
    }

    suspend fun deleteUserData() {
        if (!authInteractor.isUserSignedIn) return
        val isDeleted = MutableStateFlow(false)
        var pathSnapshotListener: ListenerRegistration? = null
        val userSnapshotListener = firestore
            .collection(UsersCollectionPath)
            .document(authInteractor.userUUID!!)
            .addSnapshotListener { userSnapshot, userError ->
                if (userError != null) {
                    Timber.d("Error while deleting user data: ${userError.message}")
                } else {
                    val domainSessionIds = (userSnapshot!!["sessions"] as List<Map<String, Any>>?)
                        ?.map { it["uuid"] as String? } ?: emptyList()
                    if (domainSessionIds.firstOrNull() != null) { // at least one not null element
                        pathSnapshotListener = firestore
                            .collection(PathsCollectionPath)
                            .whereIn("sessionUUID", domainSessionIds)
                            .addSnapshotListener { pathSnapshot, pathError ->
                                if (pathError != null) {
                                    Timber.d("Error while deleting user data: ${pathError.message}")
                                } else {
                                    Timber.d("Delete data for user ${authInteractor.userUUID}")
                                    pathSnapshot!!.documents.forEach {
                                        it.reference.delete()
                                    }
                                    userSnapshot.reference.delete()
                                    isDeleted.value = true
                                }
                            }
                    }
                }
            }
        isDeleted.collectLatest {
            if (it) {
                userSnapshotListener.remove()
                pathSnapshotListener?.remove()
            }
        }
    }

    companion object {
        const val UsersCollectionPath = "users"
        const val SessionsCollectionPath = "sessions"
        const val PathsCollectionPath = "paths"
    }
}