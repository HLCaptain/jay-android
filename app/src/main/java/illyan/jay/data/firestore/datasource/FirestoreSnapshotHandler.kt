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
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

abstract class FirestoreSnapshotHandler<DataType, SnapshotType> {
    abstract val snapshotSourceFlow: Flow<Flow<SnapshotType>?>

    protected val documentReferences = MutableStateFlow<List<DocumentReference>?>(null)
    val snapshots = channelFlow {
        var uuidJob: Job? = null
        snapshotSourceFlow.collectLatest { snapshots ->
            resetState()
            uuidJob?.cancel(CancellationException("User authentication changed"))
            snapshots?.let {
                uuidJob = launch {
                    snapshots.collectLatest { send(it) }
                }
            }
        }
    }

    abstract fun toObject(snapshot: SnapshotType): Pair<DataType?, SnapshotMetadata>
    abstract fun references(): Flow<List<DocumentReference>>

    fun dataObjects(): Flow<Pair<DataType?, SnapshotMetadata>> {
        return snapshots.map { toObject(it) }
    }

    suspend fun deleteReferences(batch: WriteBatch? = null) {
        references().first { references ->
            if (batch != null) {
                references.forEach { batch.delete(it) }
            } else {
                references.forEach(DocumentReference::delete)
            }
            true
        }
    }

    private fun resetState() {
        documentReferences.update { null }
    }
}