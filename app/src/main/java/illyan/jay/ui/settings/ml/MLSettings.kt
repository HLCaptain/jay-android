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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.JayDialogSurface
import illyan.jay.ui.components.MediumCircularProgressIndicator
import illyan.jay.ui.components.PreviewAccessibility
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.settings.ml.model.ModelState
import illyan.jay.ui.settings.ml.model.UiModel
import illyan.jay.ui.settings.user.BasicSetting
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun MLSettingsDialogScreen(
    viewModel: MLSettingsViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val uiModels by viewModel.models.collectAsStateWithLifecycle()
    MLSettingsDialogContent(
        uiModels = uiModels,
        onRefreshModelList = viewModel::refreshModelList,
        onDeleteAllModels = viewModel::onDeleteAllModels,
        onDownloadModel = viewModel::downloadModel,
    )
}

@Composable
fun MLSettingsDialogContent(
    modifier: Modifier = Modifier,
    uiModels: List<UiModel> = emptyList(),
    onRefreshModelList: () -> Unit = {},
    onDeleteAllModels: () -> Unit = {},
    onDownloadModel: (String) -> Unit = {},
) {
    JayDialogContent(
        modifier = modifier,
        title = {
            Text(text = stringResource(R.string.machine_learning))
        },
        text = {
            MLSettingsScreen(
                uiModels = uiModels,
                downloadModel = onDownloadModel,
            )
        },
        containerColor = Color.Transparent,
        buttons = {
            MLSettingsButtons(
                modifier = Modifier.fillMaxWidth(),
                onRefreshModelList = onRefreshModelList,
                onDeleteAllModels = onDeleteAllModels,
            )
        }
    )
}

@Composable
fun MLSettingsScreen(
    modifier: Modifier = Modifier,
    uiModels: List<UiModel> = emptyList(),
    downloadModel: (String) -> Unit = {},
) {
    Crossfade(targetState = uiModels.isEmpty(), label = "ML Model list") { isEmpty ->
        if (isEmpty) {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.no_available_models_found),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(modifier = modifier) {
                items(uiModels) {
                    BasicSetting(
                        title = it.name,
                        label = {
                            Crossfade(
                                modifier = Modifier.animateContentSize(),
                                targetState = it.state,
                                label = "ML Model download state"
                            ) { state ->
                                when (state) {
                                    ModelState.Available -> TextButton(onClick = { downloadModel(it.name) }) {
                                        Text(text = stringResource(R.string.download))
                                        Icon(imageVector = Icons.Rounded.Download, contentDescription = "")
                                    }
                                    ModelState.Downloading -> TextButton(onClick = {}) {
//                                Text(text = stringResource(R.string.downloading))
                                        MediumCircularProgressIndicator()
                                    }
                                    ModelState.Downloaded -> TextButton(onClick = {}) {
//                                Text(text = stringResource(R.string.downloaded))
                                        Icon(imageVector = Icons.Rounded.Done, contentDescription = "")
                                    }
                                }
                            }
                        },
                        onClick = { if (it.state == ModelState.Available) downloadModel(it.name) },
                    )
                }
            }
        }
    }
}

@Composable
fun MLSettingsButtons(
    modifier: Modifier = Modifier,
    onRefreshModelList: () -> Unit = {},
    onDeleteAllModels: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onDeleteAllModels) {
            Text(text = stringResource(R.string.delete_all))
            Icon(imageVector = Icons.Rounded.Delete, contentDescription = "")
        }
        TextButton(onClick = onRefreshModelList) {
            Text(text = stringResource(R.string.refresh))
            Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "")
        }
    }
}

@PreviewAccessibility
@Composable
fun MLSettingsDialogContentPreview() {
    JayTheme {
        JayDialogSurface {
            MLSettingsDialogContent(
                uiModels = listOf(
                    UiModel("Model 1", ModelState.Available),
                    UiModel("Model 2", ModelState.Downloading),
                    UiModel("Model 3", ModelState.Downloaded),
                )
            )
        }
    }
}
