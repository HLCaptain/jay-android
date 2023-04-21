package illyan.jay.data.firestore.datasource

import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import illyan.jay.data.firestore.model.FirestoreUser
import illyan.jay.di.UserSnapshotHandler
import illyan.jay.domain.interactor.AuthInteractor
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserFirestoreDataSource @Inject constructor(
    firestore: FirebaseFirestore,
    appLifecycle: Lifecycle,
    coroutineScopeIO: CoroutineScope,
    @UserSnapshotHandler snapshotHandler: FirestoreSnapshotHandler<FirestoreUser, DocumentSnapshot>,
): BaseFirestoreDataStore<FirestoreUser, DocumentSnapshot>(
    firestore = firestore,
    appLifecycle = appLifecycle,
    coroutineScopeIO = coroutineScopeIO,
    snapshotHandler = snapshotHandler,
)