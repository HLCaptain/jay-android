package illyan.jay.data.firestore.datasource

import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import illyan.jay.data.firestore.model.FirestoreUser
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.di.UserSnapshotHandler
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserFirestoreDataFlow @Inject constructor(
    firestore: FirebaseFirestore,
    appLifecycle: Lifecycle,
    @CoroutineScopeIO coroutineScopeIO: CoroutineScope,
    @UserSnapshotHandler snapshotHandler: FirestoreSnapshotHandler<FirestoreUser, DocumentSnapshot>,
) : FirestoreDataFlow<FirestoreUser, FirestoreUser>(
    firestore = firestore,
    appLifecycle = appLifecycle,
    coroutineScopeIO = coroutineScopeIO,
    toDomainModel = { it },
    snapshotHandler = snapshotHandler,
)