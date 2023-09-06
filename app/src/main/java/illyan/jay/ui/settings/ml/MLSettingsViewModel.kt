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

package illyan.jay.ui.settings.ml

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.ModelInteractor
import illyan.jay.ui.settings.ml.model.ModelState
import illyan.jay.ui.settings.ml.model.UiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MLSettingsViewModel @Inject constructor(
    private val modelInteractor: ModelInteractor,
) : ViewModel() {
    private val availableModels = modelInteractor.availableModels
    private val downloadingModels = modelInteractor.downloadingModels
    private val downloadedModels = modelInteractor.downloadedModels
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val models = combine(
        availableModels,
        downloadedModels,
        downloadingModels
    ) { availableModels, downloaded, downloading ->
        availableModels.map { availableModel ->
            val downloadedModel = downloaded.firstOrNull { it.name == availableModel }
            val downloadingModel = downloading.firstOrNull { it == availableModel }
            val state = when {
                downloadedModel != null -> ModelState.Downloaded
                downloadingModel != null -> ModelState.Downloading
                else -> ModelState.Available
            }
            UiModel(availableModel, state)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refreshModelList() {
        modelInteractor.refreshAvailableModelsList()
    }

    fun downloadModel(modelName: String) {
        modelInteractor.getModel(modelName)
    }

    fun onDeleteAllModels() {
        viewModelScope.launch {
            modelInteractor.deleteAllModels()
        }
    }
}