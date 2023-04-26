package illyan.jay.data.resolver

import androidx.datastore.core.DataStore
import illyan.jay.data.DataStatus
import illyan.jay.data.datastore.model.AppSettings
import illyan.jay.data.firestore.datasource.PreferencesFirestoreDataSource
import illyan.jay.data.room.datasource.PreferencesRoomDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class PreferencesResolver @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val appSettingsDataStore: DataStore<AppSettings>,
    private val preferencesFirestoreDataSource: PreferencesFirestoreDataSource,
    private val preferencesRoomDataSource: PreferencesRoomDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
) : DataResolver<DomainPreferences>(
    coroutineScopeIO = coroutineScopeIO
) {
    override val enableSyncedData = authInteractor.isUserSignedInStateFlow
    override val cloudDataStatus = preferencesFirestoreDataSource.cloudPreferencesStatus

    override val localDataStatus: StateFlow<DataStatus<DomainPreferences>> by lazy {
        val statusStateFlow = MutableStateFlow(DataStatus<DomainPreferences>())
        Timber.v("Refreshing local user preferences data collection")
        var dataCollectionJob: Job? = null
        coroutineScopeIO.launch {
            authInteractor.userUUIDStateFlow.collectLatest { uuid ->
                statusStateFlow.update { DataStatus(data = null, isLoading = true) }
                dataCollectionJob?.cancel(CancellationException("User Authentication changed, need to cancel jobs depending on User Authentication"))
                if (uuid != null) { // User signed in
                    Timber.v("Collecting signed in user preferences from disk")
                    dataCollectionJob = coroutineScopeIO.launch {
                        preferencesRoomDataSource.getPreferences(uuid).collectLatest { preferences ->
                            statusStateFlow.update { DataStatus(preferences, false) }
                        }
                    }
                } else { // Offline user
                    Timber.v("Collecting offline user preferences from disk")
                    // Simple, we only use the baseline preferences for offline users
                    dataCollectionJob = coroutineScopeIO.launch {
                        appSettingsDataStore.data.collectLatest { settings ->
                            statusStateFlow.update { DataStatus(settings.preferences, false) }
                        }
                    }
                }
            }
        }
        statusStateFlow.asStateFlow()
    }

    override fun shouldSyncData(localData: DomainPreferences?): Boolean {
        return localData?.shouldSync ?: DomainPreferences.Default.shouldSync
    }

    override fun uploadDataToCloud(data: DomainPreferences) {
        preferencesFirestoreDataSource.setPreferences(data)
    }

    override fun upsertDataToLocal(data: DomainPreferences) {
        preferencesRoomDataSource.upsertPreferences(data)
    }

    override fun createNewDataInstance(): DomainPreferences {
        return DomainPreferences(userUUID = authInteractor.userUUID)
    }

    override fun resolve(
        localData: DomainPreferences,
        syncedData: DomainPreferences,
    ): ResolvedState {
        return if (localData == syncedData) {
            // Same lastUpdate, assuming the version is the same
            Timber.v("Both local and synced preferences are matching, returning localPreferences")
            ResolvedState.Equal
        } else if (localData.isAfter(syncedData)) {
            // If local is more fresh, then update synced preferences.
            Timber.v("Local preferences are more fresh, uploading it to cloud")
            ResolvedState.Local
        } else {
            // If synced is more fresh, then update local preferences.
            Timber.v("Synced preferences are more fresh, saving it onto disk")
            ResolvedState.Synced
        }
    }
}