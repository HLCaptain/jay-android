package illyan.jay.data.firestore.datasource

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SnapshotMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.updateAndGet

class FirestoreQuerySnapshotHandler<DataType>(
    private val snapshotToObject: (QuerySnapshot) -> DataType?,
    override val snapshotSourceFlow: Flow<Flow<QuerySnapshot>?>,
) : FirestoreSnapshotHandler<DataType, QuerySnapshot>() {

    override fun toObject(snapshot: QuerySnapshot): Pair<DataType?, SnapshotMetadata> {
        return snapshotToObject(snapshot) to snapshot.metadata
    }

    override fun references(): Flow<List<DocumentReference>> {
        return snapshots.map { snapshot ->
            documentReferences.updateAndGet { snapshot.documents.map { it.reference } }!!
        }
    }
}