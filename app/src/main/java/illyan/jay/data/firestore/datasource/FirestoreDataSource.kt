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

package illyan.jay.data.firestore.datasource

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import timber.log.Timber

abstract class FirestoreDataSource<FirestoreType : Any>(
    private val firestore: FirebaseFirestore
) {

    abstract fun getReference(data: FirestoreType): DocumentReference

    fun insertData(
        data: FirestoreType,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting data: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted data") }
    ) = insertData(
        data = listOf(data),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess
    )

    fun insertData(
        data: List<FirestoreType>,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting data: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted data") },
    ) {
        firestore.runBatch { batch ->
            insertData(data, batch)
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    fun insertData(data: List<FirestoreType>, batch: WriteBatch) {
        data.forEach { batch.set(getReference(it), it) }
    }

    fun deleteData(
        data: FirestoreType,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting data: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted data") }
    ) = deleteData(
        data = listOf(data),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess
    )

    fun deleteData(
        data: List<FirestoreType>,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting data: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted data") },
    ) {
        firestore.runBatch { batch ->
            deleteData(data, batch)
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    fun deleteData(data: List<FirestoreType>, batch: WriteBatch) {
        data.forEach { batch.delete(getReference(it)) }
    }

    fun modifyData(
        data: FirestoreType,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting data: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted data") },
        modify: (batch: WriteBatch, data: FirestoreType, reference: DocumentReference) -> Unit
    ) = modifyData(
        data = listOf(data),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess,
        modify = { batch, _ -> modify(batch, data, getReference(data)) }
    )

    fun modifyData(
        data: List<FirestoreType>,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting data: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted data") },
        modify: (batch: WriteBatch, dataWithReference: List<Pair<FirestoreType, DocumentReference>>) -> Unit
    ) {
        firestore.runBatch { batch ->
            modify(batch, data.map { it to getReference(it) })
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }.addOnCanceledListener {
            onCancel()
        }
    }
}