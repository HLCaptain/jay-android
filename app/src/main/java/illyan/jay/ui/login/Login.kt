/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import illyan.jay.R
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.JayDialogSurface
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.profile.LocalDialogActivityProvider
import illyan.jay.ui.profile.LocalDialogDismissRequest
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun LoginDialogScreen(
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsStateWithLifecycle()
    val activity = LocalDialogActivityProvider.current
    val dismissDialog = LocalDialogDismissRequest.current
    LaunchedEffect(isUserSignedIn) {
        if (isUserSignedIn) dismissDialog()
    }
    LoginDialogContent(
        modifier = Modifier.fillMaxWidth(),
        signInViaGoogle = { activity?.let { viewModel.signInViaGoogle(it) } }
    )
}

@Composable
fun LoginDialogContent(
    modifier: Modifier = Modifier,
    signInViaGoogle: () -> Unit = {},
) {
    JayDialogContent(
        modifier = modifier,
        title = { LoginTitle() },
        text = {
            LoginScreen(
                modifier = Modifier.fillMaxWidth(),
                signInViaGoogle = signInViaGoogle
            )
        },
        buttons = { LoginButtons(modifier = Modifier.fillMaxWidth()) },
        containerColor = Color.Transparent,
    )
}

@Composable
fun LoginTitle() {
    Text(text = stringResource(R.string.login_to_jay))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    signInViaGoogle: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = signInViaGoogle
        ) {
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
}

@Composable
fun LoginButtons(
    modifier: Modifier = Modifier
) {
    val onDialogClosed = LocalDialogDismissRequest.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = { onDialogClosed() }
        ) {
            Text(text = stringResource(R.string.cancel))
        }
        Button(
            onClick = {
                // TODO: Login via email/password
            },
            enabled = false,
        ) {
            Text(text = stringResource(R.string.login))
        }
    }
}

@PreviewLightDarkTheme
@Composable
private fun PreviewLoginDialog() {
    JayTheme {
        JayDialogSurface {
            LoginDialogContent()
        }
    }
}