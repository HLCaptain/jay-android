package illyan.jay.di

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import illyan.jay.data.firestore.datasource.FirestoreDocumentSnapshotHandler
import illyan.jay.data.firestore.datasource.FirestoreQuerySnapshotHandler
import illyan.jay.data.firestore.datasource.FirestoreSnapshotHandler
import illyan.jay.data.firestore.model.FirestorePath
import illyan.jay.data.firestore.model.FirestoreUser
import illyan.jay.domain.interactor.AuthInteractor
import kotlinx.coroutines.flow.map

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    @Provides
    @UserSnapshotHandler
    fun provideFirestoreUserSnapshotHandler(
        firestore: FirebaseFirestore,
        authInteractor: AuthInteractor,
    ): FirestoreSnapshotHandler<FirestoreUser, DocumentSnapshot> {
        return FirestoreDocumentSnapshotHandler(
            snapshotToObject = { it.toObject() },
            snapshotSourceFlow = authInteractor.userUUIDStateFlow.map { uuid ->
                if (uuid != null) {
                    firestore
                        .collection(FirestoreUser.CollectionName)
                        .document(uuid)
                        .snapshots(MetadataChanges.INCLUDE)
                } else {
                    null
                }
            },
            initialReference = authInteractor.userUUID?.let { uuid ->
                firestore
                    .collection(FirestoreUser.CollectionName)
                    .document(uuid)
                }
        )
    }

    @Provides
    @PathsSnapshotHandler
    fun provideFirestorePathSnapshotHandler(
        firestore: FirebaseFirestore,
        authInteractor: AuthInteractor,
    ): FirestoreSnapshotHandler<List<FirestorePath>, QuerySnapshot> {
        return FirestoreQuerySnapshotHandler(
            snapshotToObject = { it.toObjects() },
            snapshotSourceFlow = authInteractor.userUUIDStateFlow.map { uuid ->
                if (uuid != null) {
                    firestore
                        .collection(FirestorePath.CollectionName)
                        .whereEqualTo(FirestorePath.FieldOwnerUUID, uuid)
                        .snapshots(MetadataChanges.INCLUDE)
                } else {
                    null
                }
            }
        )
    }
}