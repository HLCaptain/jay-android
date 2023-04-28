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

package illyan.jay.data.resolver

import illyan.jay.data.DataStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

abstract class DataResolver<DataType>(
    private val coroutineScopeIO: CoroutineScope
) {
    abstract fun uploadDataToCloud(data: DataType)
    abstract fun upsertDataToLocal(data: DataType)
    abstract fun createNewDataInstance(): DataType
    abstract fun resolve(localData: DataType, syncedData: DataType): ResolvedState
    abstract fun shouldSyncData(localData: DataType?): Boolean

    abstract val enableSyncedData: Flow<Boolean>
    abstract val cloudDataStatus: StateFlow<DataStatus<DataType>>
    abstract val localDataStatus: StateFlow<DataStatus<DataType>>

    val localData by lazy {
        localDataStatus.map { it.data }
            .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    val cloudData by lazy {
        cloudDataStatus.map { it.data }
            .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    val localDataLoading by lazy {
        localDataStatus.map { it.isLoading }
            .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    val cloudDataLoading by lazy {
        cloudDataStatus.map { it.isLoading }
            .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    val data by lazy {
        combine(
            cloudDataStatus,
            localDataStatus,
            enableSyncedData,
        ) { syncedStatus, localStatus, enableSyncedData ->
            resolveDataState(
                syncedData = syncedStatus.data,
                localData = localStatus.data,
                isSyncedDataLoading = syncedStatus.isLoading,
                isLocalDataLoading = localStatus.isLoading,
                enableSyncedData = enableSyncedData
            )
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    val isLoading by lazy {
        combine(
            cloudDataLoading,
            localDataLoading,
            enableSyncedData
        ) { cloudDataLoading, localDataLoading, enableSyncedData ->
            if (!enableSyncedData) {
                localDataLoading
            } else {
                if (cloudDataLoading == null && localDataLoading == null) {
                    null
                } else {
                    cloudDataLoading == true || localDataLoading == true
                }
            }
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    val isDataSynced by lazy {
        combine(
            isLoading,
            localData,
            cloudData
        ) { isLoading, localData, cloudData ->
            if (isLoading == false) {
                localData == cloudData && shouldSyncData(localData)
            } else {
                false
            }
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, localData.value == cloudData.value)
    }

    val canSyncData by lazy {
        combine(
            enableSyncedData,
            isLoading,
            localData
        ) { syncEnabled, isLoading, localData ->
            syncEnabled && isLoading == false && localData != null
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, false)
    }

    val shouldSyncData by lazy {
        localData.map { shouldSyncData(it) }
            .stateIn(coroutineScopeIO, SharingStarted.Eagerly, shouldSyncData(null))
    }

    /**
     * @param enableSyncedData whether to enable the consideration of synced data or not
     */
    private fun resolveDataState(
        syncedData: DataType?,
        localData: DataType?,
        isSyncedDataLoading: Boolean?,
        isLocalDataLoading: Boolean?,
        enableSyncedData: Boolean
    ): DataType? {
        // While either is loading, preferences are null
        // If local is loaded, the preferences are local, cloud still loading
        // If cloud is loaded, the preferences are cloud, local still loading
        // If user changes preferences when either is loading, resolve requests (easier to ignore requests) when both are loaded and resolved.
        // ONLY RESOLVE ANYTHING WHEN BOTH CLOUD AND LOCAL IS LOADED (create new preferences document)

        // User don't have local nor synced preferences? Create and upload local preferences.
        // User don't have local but have synced Preferences? Use synced preferences.
        // User have local but not synced preferences? Upload local preferences.
        // User have local and synced preferences, which to use?
        // If synced is more fresh, then update local preferences.
        // If local is more fresh, then update synced version. (EASIER)

        return if (enableSyncedData) {
            if (isLocalDataLoading != false && isSyncedDataLoading != false) { // While either is loading, preferences are null
                Timber.v("While local or synced data are loading, returning null")
                null
            } else if (isLocalDataLoading == false && isSyncedDataLoading != false) { // If local is loaded, the preferences are local, cloud still loading
                Timber.v("If local is loaded and cloud is not, returning local data")
                localData
            } else if (isLocalDataLoading != false && isSyncedDataLoading == false) { // If cloud is loaded, the preferences are cloud, local still loading
                Timber.v("If cloud is loaded and local is not, returning cloud data")
                syncedData
            } else {
                if (localData == null && syncedData == null) {
                    // User don't have local nor synced preferences? Create and upload local preferences.
                    Timber.v("User doesn't have local nor synced data, create and insert one into local database")
                    val freshData = createNewDataInstance()
                    upsertDataToLocal(freshData)
                    null
                } else if (localData == null && syncedData != null) {
                    // User don't have local but have synced Preferences? Use synced preferences.
                    Timber.v("User doesn't have local but have synced data, save synced data")
                    upsertDataToLocal(syncedData)
                    syncedData
                } else if (localData != null && syncedData == null && shouldSyncData(localData)) {
                    // User have local but not synced preferences? Upload local preferences.
                    Timber.v("User has local data which need to be synced but has no data in cloud, upload local data")
                    uploadDataToCloud(localData)
                    localData
                } else { // Both sessions are now loaded in and not null
                    if (!shouldSyncData(localData)) {
                        localData
                    } else {
                        when (resolve(localData!!, syncedData!!)) {
                            ResolvedState.Equal -> {
                                localData
                            }
                            ResolvedState.Synced -> {
                                upsertDataToLocal(syncedData)
                                syncedData
                            }
                            ResolvedState.Local -> {
                                uploadDataToCloud(localData)
                                localData
                            }
                        }
                    }
                }
            }
        } else {
            Timber.v("Synced data not enabled, returning local data")
            localData
        }
    }

    enum class ResolvedState {
        /**
         * Local and Synced data are equal.
         */
        Equal,

        /**
         * Local data is more fresh.
         */
        Local,

        /**
         * Synced data is more fresh.
         */
        Synced
    }
}