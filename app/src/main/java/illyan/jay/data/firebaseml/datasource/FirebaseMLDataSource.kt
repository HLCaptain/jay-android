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

package illyan.jay.data.firebaseml.datasource

import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.util.FirebaseRemoteConfigKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMLDataSource @Inject constructor(
    private val modelDownloader: FirebaseModelDownloader,
    private val remoteConfig: FirebaseRemoteConfig,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    private val _downloadingModels = MutableStateFlow(mutableListOf<String>())
    val downloadingModels = _downloadingModels.asStateFlow()

    private val _availableModels = MutableStateFlow(getAvailableModelNames())
    val availableModels = _availableModels.asStateFlow()

    private val _downloadedModels = MutableStateFlow(listOf<CustomModel>())
    val downloadedModels = _downloadedModels.asStateFlow()

    init {
        refreshDownloadedModelList()
    }

    private fun refreshDownloadedModelList() {
        modelDownloader.listDownloadedModels()
            .addOnSuccessListener { models ->
                _downloadedModels.update { models.toList() }
            }
    }

    // TODO: Add restrictions based on authenticated user model access.
    //  In other words, only allow the user to download the models they have access to.
    fun getModel(
        modelName: String,
        conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder().build(),
        downloadType: DownloadType = DownloadType.LATEST_MODEL,
    ): SharedFlow<CustomModel?> {
        Timber.v("Downloading model: $modelName")
        val flow = MutableSharedFlow<CustomModel?>(extraBufferCapacity = 1)
        modelDownloader
            .getModel(modelName, downloadType, conditions)
            .addOnSuccessListener { model ->
                _downloadingModels.update {
                    if (model.file != null) {
                        Timber.d("Model download in progress: $modelName")
                        startedDownloadingModel(modelName)
                    } else {
                        Timber.d("Model download ended: $modelName")
                        finishedDownloadingModel(modelName)
                    }
                    it
                }
                coroutineScopeIO.launch { flow.emit(model) }
                refreshDownloadedModelList()
            }
            .addOnFailureListener {
                finishedDownloadingModel(modelName)
                coroutineScopeIO.launch { flow.emit(null) }
            }
            .addOnCanceledListener {
                finishedDownloadingModel(modelName)
                coroutineScopeIO.launch { flow.emit(null) }
            }
        return flow
    }

    private fun startedDownloadingModel(modelName: String) {
        _downloadingModels.update { models ->
            models.add(modelName)
            models.distinct().toMutableList()
        }
    }
    private fun finishedDownloadingModel(modelName: String) {
        _downloadingModels.update { models ->
            models.filter { it != modelName }
            models
        }
    }

    fun getModelId(
        modelName: String,
        conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder().build(),
        downloadType: DownloadType = DownloadType.LATEST_MODEL,
    ): SharedFlow<Long?> {
        val flow = MutableSharedFlow<Long?>(extraBufferCapacity = 1)
        modelDownloader
            .getModelDownloadId(
                modelName,
                modelDownloader.getModel(modelName, downloadType, conditions)
            )
            .addOnSuccessListener {
                Timber.v("Model ID: $it")
                coroutineScopeIO.launch { flow.emit(it) }
            }
            .addOnFailureListener {
                Timber.e(it, "Model ID query failed: ${it.message}")
                coroutineScopeIO.launch { flow.emit(null) }
            }
            .addOnCanceledListener {
                Timber.v("Model ID query cancelled.")
                coroutineScopeIO.launch { flow.emit(null) }
            }
        return flow
    }

    // TODO: restrict access to only the models the user has access to.
    fun getDownloadedModels(): SharedFlow<List<CustomModel>> {
        val flow = MutableSharedFlow<List<CustomModel>>(extraBufferCapacity = 1)
        modelDownloader.listDownloadedModels()
            .addOnSuccessListener {
                coroutineScopeIO.launch { flow.emit(it.toList()) }
            }
        return flow
    }

    private fun getAvailableModelNames() = Json.decodeFromString<List<String>>(
        remoteConfig.getString(FirebaseRemoteConfigKeys.MLAvailableModels)
    )

    fun refreshAvailableModelsList() {
        Timber.v("Refreshing available models list from Firebase Remote Config.")
        remoteConfig.addOnConfigUpdateListener(
            object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    remoteConfig.activate()
                    _availableModels.update { getAvailableModelNames() }
                    Timber.v("Available models list updated. Updating Remote Config.")
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Timber.e(error, "Error updating available models list: ${error.message}")
                    _availableModels.update {
                        emptyList()
                    }
                }
            }
        )
        remoteConfig.fetchAndActivate()
    }

    suspend fun deleteAllModels() {
        Timber.v("Deleting all downloaded models.")
        downloadedModels.first { models ->
            models.forEach { deleteModel(it.name) }
            true
        }
//        modelDownloader
//            .listDownloadedModels()
//            .addOnSuccessListener { models ->
//                models.forEach { model ->
//                    modelDownloader.deleteDownloadedModel(model.name)
//                }
//                refreshDownloadedModelList()
//            }
//            .addOnFailureListener {
//                Timber.e(it, "Error deleting downloaded models: ${it.message}")
//            }
//            .addOnCanceledListener { Timber.v("Deleting downloaded models cancelled.") }
    }

    fun deleteModel(modelName: String) {
        Timber.v("Deleting model: $modelName")
        modelDownloader.deleteDownloadedModel(modelName)
            .addOnSuccessListener { refreshDownloadedModelList() }
            .addOnFailureListener { Timber.e(it, "Error deleting downloaded model: ${it.message}") }
            .addOnCanceledListener { Timber.v("Deleting downloaded model cancelled.") }
    }
}