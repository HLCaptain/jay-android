/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.firestore.datasource

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SnapshotMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

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