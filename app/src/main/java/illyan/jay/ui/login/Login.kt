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

package illyan.jay.ui.login

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import illyan.jay.MainActivity
import illyan.jay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    isDialogOpen: Boolean = true,
    onDialogClosed: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    LaunchedEffect(isUserSignedIn) {
        if (isUserSignedIn) onDialogClosed()
    }
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { onDialogClosed() },
            title = { Text(text = stringResource(R.string.login_to_jay)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.signInViaGoogle((context as MainActivity)) }) {
                        Text(text = stringResource(R.string.google_sign_in))
                    }
                    // TODO: make login via email/password combo
                    var email by remember { mutableStateOf("") }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        enabled = false,
                        onValueChange = {
                            email = it
                        },
                        label = {
                            Text(text = stringResource(R.string.email))
                        }
                    )
                    var password by remember { mutableStateOf("") }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        enabled = false,
                        onValueChange = {
                            password = it
                        },
                        label = {
                            Text(text = stringResource(R.string.password))
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Login via email/password
                    },
                    enabled = false,
                ) {
                    Text(text = stringResource(R.string.login))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDialogClosed()
                }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}