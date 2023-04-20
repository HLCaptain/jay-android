package illyan.jay.data.firestore.datasource

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

abstract class FirestoreSnapshotHandler<DataType, SnapshotType>(
    private val metadataChanges: MetadataChanges = MetadataChanges.EXCLUDE
) {

    protected val documentReferences = MutableStateFlow<List<DocumentReference>?>(null)

    abstract fun toObject(snapshot: SnapshotType): Pair<DataType, SnapshotMetadata>
    abstract fun snapshot(metadataChanges: MetadataChanges = this.metadataChanges): Flow<SnapshotType>
    abstract fun references(): Flow<List<DocumentReference>>

    fun dataObjects(): Flow<Pair<DataType, SnapshotMetadata>> {
        return snapshot().map { toObject(it) }
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

    fun resetReferences() {
        documentReferences.update { null }
    }
}