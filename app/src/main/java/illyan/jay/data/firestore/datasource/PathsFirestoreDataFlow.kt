package illyan.jay.data.firestore.datasource

import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import illyan.jay.data.firestore.model.FirestorePath
import illyan.jay.data.firestore.toDomainLocations
import illyan.jay.di.PathsSnapshotHandler
import illyan.jay.domain.model.DomainLocation
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathsFirestoreDataFlow @Inject constructor(
    firestore: FirebaseFirestore,
    appLifecycle: Lifecycle,
    coroutineScopeIO: CoroutineScope,
    @PathsSnapshotHandler snapshotHandler: FirestoreSnapshotHandler<List<FirestorePath>, QuerySnapshot>,
) : FirestoreDataFlow<List<FirestorePath>, List<List<DomainLocation>>>(
    firestore = firestore,
    appLifecycle = appLifecycle,
    coroutineScopeIO = coroutineScopeIO,
    toDomainModel = { paths ->
        paths
            ?.groupBy { it.sessionUUID }
            ?.map { it.value.toDomainLocations() }
    },
    snapshotHandler = snapshotHandler,
)