package illyan.jay.data.firestore.datasource

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import illyan.jay.data.DataStatus
import illyan.jay.util.runBatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

abstract class FirestoreDataFlow<FirestoreType, DomainType>(
    private val firestore: FirebaseFirestore,
    private val coroutineScopeIO: CoroutineScope,
    private val toDomainModel: (FirestoreType?) -> DomainType?,
    private val snapshotHandler: FirestoreSnapshotHandler<FirestoreType, out Any>,
    private val appLifecycle: Lifecycle? = null,
) : DefaultLifecycleObserver {

    private val _dataListenerJob = MutableStateFlow<Job?>(null)
    private val _dataStatus = MutableStateFlow(DataStatus<DomainType>())
    private val _cloudDataStatus = MutableStateFlow(DataStatus<DomainType>())

    val dataStatus: StateFlow<DataStatus<DomainType>> by lazy { _dataStatus.asStateFlow() }
    val data = dataStatus.map { it.data }
        .stateIn(coroutineScopeIO, SharingStarted.Eagerly, dataStatus.value.data)
    val dataLoading = dataStatus.map { it.isLoading }
        .stateIn(coroutineScopeIO, SharingStarted.Eagerly, dataStatus.value.isLoading)

    val cloudDataStatus: StateFlow<DataStatus<DomainType>> by lazy { _cloudDataStatus.asStateFlow() }
    val cloudData = cloudDataStatus.map { it.data }
        .stateIn(coroutineScopeIO, SharingStarted.Eagerly, cloudDataStatus.value.data)
    val cloudDataLoading = cloudDataStatus.map { it.isLoading }
        .stateIn(coroutineScopeIO, SharingStarted.Eagerly, cloudDataStatus.value.isLoading)

    init {
        init()
    }

    fun init() {
        appLifecycle?.addObserver(this)
        resetDataListenerState()
        refreshData()
    }

    private fun resetDataListenerState() {
        Timber.v("Resetting data listener state")
        _dataListenerJob.value?.cancel(CancellationException("Data listener reset requested, cancelling ongoing job"))
        _dataListenerJob.update { null }
        _dataStatus.update { DataStatus() }
        _cloudDataStatus.update { DataStatus() }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.d("Reload data on App Lifecycle Start")
        resetDataListenerState()
        refreshData()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.d("Remove data listener on App Lifecycle Stop")
        resetDataListenerState()
    }

    private fun refreshData() {
        Timber.v("Refreshing data requested")
        if (_cloudDataStatus.value.isLoading == true) {
            Timber.d("Not refreshing data, due to another being loaded in or prerequisites are not met")
            return
        }
        resetDataListenerState()
        _dataStatus.update { it.copy(isLoading = true) }
        _cloudDataStatus.update { it.copy(isLoading = true) }
        _dataListenerJob.value?.cancel(CancellationException("Refreshing data, launching new job to handle updates, old job is cancelled"))
        _dataListenerJob.update {
            coroutineScopeIO.launch {
                snapshotHandler.dataObjects().collectLatest { (data, metadata) ->
                    Timber.v("New snapshot from ${if (metadata.isFromCache) "Cache" else "Cloud"}")
                    processData(
                        data = toDomainModel(data),
                        isFromCache = metadata.isFromCache,
                        updateCachedDataStatus = { status -> _dataStatus.update { status } },
                        updateCloudDataStatus = { status -> _cloudDataStatus.update { status } },
                    )
                }
            }
        }
    }

    private fun processData(
        data: DomainType?,
        isFromCache: Boolean,
        updateCachedDataStatus: (DataStatus<DomainType>) -> Unit,
        updateCloudDataStatus: (DataStatus<DomainType>) -> Unit,
    ) {
        // Cache
        if (data != null) {
            if (_dataStatus.value.data != null) {
                Timber.v("Refreshing Cached data")
            } else {
                Timber.d("Firestore loaded data from Cache")
            }
            updateCachedDataStatus(DataStatus(data = data, isLoading = false))
        } else {
            updateCachedDataStatus(DataStatus(data = null, isLoading = false))
        }

        // Cloud
        if (!isFromCache) {
            if (data != null) {
                if (_cloudDataStatus.value.data != null) {
                    Timber.v("Firestore loaded fresh data from Cloud")
                } else {
                    Timber.d("Firestore loaded data from Cloud")
                }
                updateCloudDataStatus(DataStatus(data = data, isLoading = false))
            } else {
                updateCloudDataStatus(DataStatus(data = null, isLoading = false))
            }
        }
    }

    suspend fun deleteData(
        onCancel: () -> Unit = { Timber.i("User deletion canceled") },
        onFailure: (Exception) -> Unit = { Timber.e(it) },
        onSuccess: () -> Unit = { Timber.i("User deletion successful") },
    ) {
        firestore.runBatch(1) { batch, onOperationFinished ->
            deleteData(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    suspend fun deleteData(
        batch: WriteBatch,
        onWriteFinished: () -> Unit = {},
    ) {
        snapshotHandler.deleteReferences(batch)
        onWriteFinished()
    }
}