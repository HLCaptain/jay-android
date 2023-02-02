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

package illyan.jay.ui.profile

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import illyan.jay.R
import illyan.jay.ui.home.AvatarAsyncImage
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.login.LoginDialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileDialog(
    isDialogOpen: Boolean = true,
    onDialogClosed: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    if (isDialogOpen) {
        val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        val isUserSigningOut by viewModel.isUserSigningOut.collectAsState()
        AlertDialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .width(screenWidthDp - 72.dp),
            onDismissRequest = { onDialogClosed() },
            title = { ProfileTitleScreen(viewModel = viewModel) },
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
                ProfileDetailsScreen(
                    context = context,
                    viewModel = viewModel
                )
            }
        )
        LoginDialog(
            isDialogOpen = showDialog,
            onDialogClosed = { showDialog = false },
            context = context
        )
    }
}

@Composable
fun ProfileTitleScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = stringResource(R.string.profile))
        AvatarAsyncImage(
            modifier = Modifier
                .size(RoundedCornerRadius * 3)
                .clip(CircleShape),
            enabled = isUserSignedIn && userPhotoUrl != null,
            userPhotoUrl = userPhotoUrl
        )
    }
}

@Composable
fun ProfileDetailsScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    context: Context,
) {
    val email by viewModel.userEmail.collectAsState()
    val phone by viewModel.userPhoneNumber.collectAsState()
    val name by viewModel.userName.collectAsState()
    var showConfidentialInfo by remember { mutableStateOf(false) }
    var authenticated by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = !name.isNullOrBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = stringResource(R.string.name) + ": ")
                    Text(
                        text = if (showConfidentialInfo) {
                            name ?: stringResource(R.string.unknown)
                        } else {
                            stringResource(R.string.hidden_field_string)
                        }
                    )
                }
            }
            AnimatedVisibility(visible = !email.isNullOrBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = stringResource(R.string.email) + ": ")
                    Text(
                        text = if (showConfidentialInfo) {
                            email ?: stringResource(R.string.unknown)
                        } else {
                            stringResource(R.string.hidden_field_string)
                        }
                    )
                }
            }
            AnimatedVisibility(visible = !phone.isNullOrBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = stringResource(R.string.phone_number) + ": ")
                    Text(
                        text = if (showConfidentialInfo) {
                            name ?: stringResource(R.string.unknown)
                        } else {
                            stringResource(R.string.hidden_field_string)
                        }
                    )
                }
            }
        }
        val anyConfidentialInfo = !name.isNullOrBlank() ||
                !email.isNullOrBlank() ||
                !phone.isNullOrBlank()
        AnimatedVisibility(visible = anyConfidentialInfo) {
            IconButton(
                onClick = {
                    toggleConfidentialInfoVisibility(
                        showConfidentialInfo = showConfidentialInfo,
                        authenticated = authenticated,
                        fragmentActivity = context as FragmentActivity,
                        onAuthenticationChanged = { authenticated = it },
                        onInfoVisibilityChanged = { showConfidentialInfo = it }
                    )
                }
            ) {
                Icon(
                    imageVector = if (showConfidentialInfo) {
                        Icons.Rounded.LockOpen
                    } else {
                        Icons.Rounded.Lock
                    },
                    contentDescription = ""
                )
            }
        }
    }
}

fun toggleConfidentialInfoVisibility(
    showConfidentialInfo: Boolean,
    authenticated: Boolean,
    fragmentActivity: FragmentActivity,
    onAuthenticationChanged: (Boolean) -> Unit,
    onInfoVisibilityChanged: (Boolean) -> Unit,
) {
    if (showConfidentialInfo) {
        onInfoVisibilityChanged(false)
    } else {
        if (authenticated) {
            onInfoVisibilityChanged(true)
        } else {
            onInfoVisibilityChanged(true)
            onAuthenticationChanged(true)
            // TODO: implement some kind of biometric authentication with multiple local users
            //  in mind. As a phone can be used by multiple people, using biometrics and other
            //  local authentication, authentication should be independent from the device itself.
            //  Though this problem is solved by Firebase Auth. Maybe simply hiding the fields is
            //  enough for now.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                val biometricManager = BiometricManager.from(fragmentActivity)
//                when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
//                    BiometricManager.BIOMETRIC_SUCCESS ->
//                        Timber.d("App can authenticate using biometrics.")
//                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
//                        Timber.e("No biometric features available on this device.")
//                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
//                        Timber.e("Biometric features are currently unavailable.")
//                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
//                        // Prompts the user to create credentials that your app accepts.
//                        val enrollIntent = Intent(ACTION_BIOMETRIC_ENROLL).apply {
//                            putExtra(EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                                BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
//                        }
//                        fragmentActivity.startActivityForResult(enrollIntent, 0)
//                    }
//                }
//                val biometricPrompt = BiometricPrompt(
//                    fragmentActivity,
//                    object : BiometricPrompt.AuthenticationCallback() {
//                        override fun onAuthenticationSucceeded(
//                            result: BiometricPrompt.AuthenticationResult,
//                        ) {
//                            super.onAuthenticationSucceeded(result)
//                            onAuthenticationChanged(true)
//                            onInfoVisibilityChanged(true)
//                        }
//                        override fun onAuthenticationFailed() {
//                            super.onAuthenticationFailed()
//                            Timber.d("Authentication failed!")
//                        }
//                        override fun onAuthenticationError(
//                            errorCode: Int,
//                            errString: CharSequence
//                        ) {
//                            super.onAuthenticationError(errorCode, errString)
//                            Timber.d("Authentication error code: $errorCode\n" +
//                                    "Error message: $errString")
//                        }
//                    })
//                val promptTitle = fragmentActivity.getString(R.string.show_profile_info)
//                val promptSubtitle =
//                    fragmentActivity.getString(R.string.authenticate_to_view_account_information)
//                val promptInfo = BiometricPrompt.PromptInfo.Builder()
//                    .setTitle(promptTitle)
//                    .setSubtitle(promptSubtitle)
//                    .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
//                    .build()
//                biometricPrompt.authenticate(promptInfo)
//            } else {
//                // TODO: make this compatible down to API 21
//                onAuthenticationChanged(true)
//            }
        }
    }
}
