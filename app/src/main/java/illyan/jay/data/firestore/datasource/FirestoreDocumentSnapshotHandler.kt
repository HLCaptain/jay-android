package illyan.jay.data.firestore.datasource

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.ktx.snapshots
import illyan.jay.di.CoroutineScopeIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class FirestoreDocumentSnapshotHandler<DataType>(
    private val snapshotToObject: (DocumentSnapshot) -> DataType?,
    override val snapshotSourceFlow: Flow<Flow<DocumentSnapshot>?>,
    private val initialReference: DocumentReference? = null,
) : FirestoreSnapshotHandler<DataType, DocumentSnapshot>() {

    init {
        initialReference?.let { documentReferences.update { listOf(initialReference) } }
    }

    override fun toObject(snapshot: DocumentSnapshot): Pair<DataType?, SnapshotMetadata> {
        return snapshotToObject(snapshot) to snapshot.metadata
    }

    override fun references(): Flow<List<DocumentReference>> {
        return snapshots.map { snapshot ->
            documentReferences.updateAndGet { listOf(snapshot.reference) }!!
        }
    }
}