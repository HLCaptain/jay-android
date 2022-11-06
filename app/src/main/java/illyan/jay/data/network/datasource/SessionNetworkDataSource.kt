/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.maps.android.ktx.utils.sphericalPathLength
import illyan.jay.data.disk.datasource.SessionDiskDataSource
import illyan.jay.data.network.toDomainLocations
import illyan.jay.data.network.toDomainSession
import illyan.jay.data.network.toHashMap
import illyan.jay.data.network.toPaths
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val sessionDiskDataSource: SessionDiskDataSource,
) {
    fun getLocations(
        sessionId: String,
        coroutineScope: CoroutineScope,
    ) = flow<List<DomainLocation>> {
        if (authInteractor.isUserSignedIn) {
            firestore
                .collection(PathsCollectionPath)
                .whereEqualTo(
                    "owner",
                    "$UsersCollectionPath/${authInteractor.userUUID}"
                )
                .whereEqualTo(
                    "partOfSession",
                    "$SessionsCollectionPath/$sessionId"
                )
                .get()
                .addOnSuccessListener { snapshot ->
                    coroutineScope.launch { emit(snapshot.documents.toDomainLocations()) }
                }
                .addOnCanceledListener { coroutineScope.launch { emit(emptyList()) } }
                .addOnFailureListener { coroutineScope.launch { emit(emptyList()) } }
        } else {
            emit(emptyList())
        }
    }

    fun getSessions(
        activity: Activity,
        listener: (List<DomainSession>) -> Unit
    ) {
        if (authInteractor.isUserSignedIn) {
            Timber.d("Connecting snapshot listener to Firebase")
            firestore
                .collection(UsersCollectionPath)
                .document(authInteractor.userUUID.toString())
                .addSnapshotListener(activity) { snapshot, error ->
                    val domainSessions = (snapshot?.get("sessions") as List<Map<String, Any>>?)?.map {
                        it.toDomainSession(it["id"] as String?)
                    } ?: emptyList()
                    Timber.d("Firebase got sessions with IDs: ${
                        domainSessions.map { it.uuid.toString().substring(0..3) }
                    }")
                    listener(domainSessions)
                }
        } else {
            Timber.d("Connecting snapshot listener to Firebase")
            listener(emptyList())
        }
    }

    fun insertSession(
        domainSession: DomainSession,
        domainLocations: List<DomainLocation>,
        coroutineScope: CoroutineScope,
    ) {
        // TODO: upload path data first
        // TODO: upload session data
        // TODO: update/upload user data with it
        // TODO: don't assume to update session data

        if (domainSession.uuid == null && authInteractor.isUserSignedIn) {
            domainSession.uuid = UUID.randomUUID().toString()
            if (domainSession.distance == null) {
                domainSession.distance = domainLocations
                    .sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
                    .map { it.latLng }.sphericalPathLength().toFloat()
            }
            val paths = domainLocations.toPaths(domainSession.uuid.toString())

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
                    mapOf("sessions" to FieldValue.arrayUnion(domainSession.toHashMap())),
                    SetOptions.merge()
                )
            }.addOnSuccessListener {
                coroutineScope.launch(Dispatchers.IO) {
                    sessionDiskDataSource.saveSession(domainSession)
                }
            }
        } // else it is already synced to the cloud

    }

    fun deleteUserData(coroutineScope: CoroutineScope) {
        if (authInteractor.isUserSignedIn) {
            firestore
                .collection(UsersCollectionPath)
                .document(authInteractor.userUUID!!)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    val domainSessionIds = (userSnapshot["sessions"] as List<Map<String, Any>>?)
                        ?.map { it["id"] as String? } ?: emptyList()
                    if (domainSessionIds.firstOrNull() != null) { // at least one not null element
                        firestore
                            .collection(PathsCollectionPath)
                            .whereIn("sessionId", domainSessionIds)
                            .get()
                            .addOnSuccessListener { pathSnapshot ->
                                pathSnapshot.documents.forEach {
                                    it.reference.delete()
                                }
                                userSnapshot.reference.delete()
                            }
                    }
                }
        }
    }

    companion object {
        const val UsersCollectionPath = "users"
        const val SessionsCollectionPath = "sessions"
        const val PathsCollectionPath = "paths"
    }
}