package illyan.jay.data.firestore.datasource

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FirestoreDocumentSnapshotHandler<DataType>(
    private val snapshotToObject: (DocumentSnapshot) -> DataType,
    private val documentReference: DocumentReference
) : FirestoreSnapshotHandler<DataType, DocumentSnapshot>() {

    init {
        documentReferences.update { listOf(documentReference) }
    }

    override fun toObject(snapshot: DocumentSnapshot): Pair<DataType, SnapshotMetadata> {
        return snapshotToObject(snapshot) to snapshot.metadata
    }

    override fun snapshot(metadataChanges: MetadataChanges): Flow<DocumentSnapshot> {
        return documentReference.snapshots(metadataChanges)
    }

    override fun references(): Flow<List<DocumentReference>> {
        return snapshot().map { listOf(it.reference) }
    }
}