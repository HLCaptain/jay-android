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

package illyan.jay.domain.interactor

import android.hardware.Sensor
import android.os.Build
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import illyan.jay.data.firebaseml.datasource.FirebaseMLDataSource
import illyan.jay.data.sensor.SensorFusion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.tensorflow.lite.Interpreter
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelInteractor @Inject constructor(
    private val firebaseMLDataSource: FirebaseMLDataSource,
    private val sensorEventInteractor: SensorEventInteractor,
) {
    val downloadingModels = firebaseMLDataSource.downloadingModels
    val availableModels = firebaseMLDataSource.availableModels
    val downloadedModels = firebaseMLDataSource.downloadedModels

    fun getModel(
        modelName: String,
        conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder().build(),
        downloadType: DownloadType = DownloadType.LATEST_MODEL,
    ) = firebaseMLDataSource.getModel(modelName, conditions, downloadType)

    fun getModelId(
        modelName: String,
        conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder().build(),
        downloadType: DownloadType = DownloadType.LATEST_MODEL,
    ) = firebaseMLDataSource.getModelId(modelName, conditions, downloadType)

    fun getDownloadedModels() = firebaseMLDataSource.getDownloadedModels()

    fun refreshAvailableModelsList() = firebaseMLDataSource.refreshAvailableModelsList()

    suspend fun deleteAllModels() = firebaseMLDataSource.deleteAllModels()

    fun deleteModel(modelName: String) = firebaseMLDataSource.deleteModel(modelName)

    suspend fun getFilteredDriverAggression(
        modelName: String,
        sessionUUID: String
    ): Flow<List<Pair<ZonedDateTime, Double>>> = flow {
        getModel(modelName).first { model ->
            val modelFile = model?.file
            if (modelFile != null) {
                val sensorEvents = sensorEventInteractor.getSensorEvents(sessionUUID).first()
                val advancedImuSensorData = SensorFusion.fuseSensors(
                    accRaw = sensorEvents.filter {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            it.type.toInt() == Sensor.TYPE_ACCELEROMETER_UNCALIBRATED
                        else
                            false
                    },
                    accSmooth = sensorEvents.filter { it.type.toInt() == Sensor.TYPE_ACCELEROMETER },
                    dirX = emptyList(),
                    dirY = emptyList(),
                    dirZ = emptyList(),
                    angVel = emptyList(),
                    angAccel = emptyList(),
                )
                val interpreter = Interpreter(modelFile)
                // TODO: Use advancedImuSensorData and get filtered driver aggression

                // TODO: emit model outputs
                //  val aggressions = mutableListOf<Pair<ZonedDateTime, Double>>()
                //  // ...Run Model on Chunk
                //  // ...Add Model output to aggressions (zonedDateTime to modelOutput)
                //  // ...emit(aggressions)
            }
            modelFile != null // The model is downloaded
        }
    }
}