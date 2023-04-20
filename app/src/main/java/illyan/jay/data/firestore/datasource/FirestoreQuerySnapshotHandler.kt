package illyan.jay.data.firestore.datasource

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FirestoreQuerySnapshotHandler<DataType>(
    private val snapshotToObject: (QuerySnapshot) -> DataType,
    private val query: Query
) : FirestoreSnapshotHandler<DataType, QuerySnapshot>() {
    override fun toObject(snapshot: QuerySnapshot): Pair<DataType, SnapshotMetadata> {
        return snapshotToObject(snapshot) to snapshot.metadata
    }

    override fun snapshot(metadataChanges: MetadataChanges): Flow<QuerySnapshot> {
        return query.snapshots(metadataChanges)
    }

    override fun references(): Flow<List<DocumentReference>> {
        return snapshot().map { snapshot ->
            val references = snapshot.documents.map { it.reference }
            documentReferences.update { references }
            references
        }
    }
}