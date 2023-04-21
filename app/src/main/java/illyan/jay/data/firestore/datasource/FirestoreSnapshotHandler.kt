package illyan.jay.data.firestore.datasource

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class FirestoreSnapshotHandler<DataType, SnapshotType> {
    abstract val snapshotSourceFlow: Flow<Flow<SnapshotType>?>

    protected val documentReferences = MutableStateFlow<List<DocumentReference>?>(null)
    val snapshots = flow {
        snapshotSourceFlow.collectLatest {
            resetState()
            it?.let { emitAll(it) }
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