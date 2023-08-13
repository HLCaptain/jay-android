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

import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import illyan.jay.data.firebaseml.datasource.FirebaseMLDataSource
import illyan.jay.di.CoroutineScopeIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelInteractor @Inject constructor(
    private val firebaseMLDataSource: FirebaseMLDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    fun getModel(
        modelName: String,
        conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder().build(),
        downloadType: DownloadType = DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
    ) = flow {
        firebaseMLDataSource.getModel(modelName, conditions, downloadType) { model ->
            if (model != null) coroutineScopeIO.launch { emit(model) }
        }
    }

    fun getModelId(
        modelName: String,
        conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder().build(),
        downloadType: DownloadType = DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
    ) = firebaseMLDataSource.getModelId(modelName, conditions, downloadType)
}