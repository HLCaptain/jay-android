/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.profile

import androidx.compose.animation.Crossfade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import illyan.jay.R
import illyan.jay.ui.login.LoginDialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileScreen(
    isDialogOpen: Boolean = true,
    onDialogClosed: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    if (isDialogOpen) {
        val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        val isUserSigningOut by viewModel.isUserSigningOut.collectAsState()
        AlertDialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            onDismissRequest = { onDialogClosed() },
            title = { Text(text = stringResource(R.string.profile)) },
            confirmButton = {
                Crossfade(targetState = isUserSignedIn) {
                    if (it) {
                        TextButton(
                            enabled = !isUserSigningOut,
                            onClick = { viewModel.signOut() }
                        ) {
                            Text(text = stringResource(R.string.sign_out))
                        }
                    } else {
                        Button(
                            onClick = { showDialog = true }
                        ) {
                            Text(text = stringResource(R.string.sign_in))
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onDialogClosed() }) {
                    Text(text = stringResource(R.string.close))
                }
            },
            text = {

            }
        )

        LoginDialog(
            isDialogOpen = showDialog,
            onDialogClosed = { showDialog = false },
            context = context
        )
    }
}
