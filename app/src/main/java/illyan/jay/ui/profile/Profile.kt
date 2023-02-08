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

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import illyan.jay.R
import illyan.jay.ui.components.AvatarAsyncImage
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.login.LoginDialog
import illyan.jay.ui.theme.JayTheme

@Composable
fun ProfileDialog(
    isDialogOpen: Boolean = true,
    onDialogClosed: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsStateWithLifecycle()
    var showLoginDialog by remember { mutableStateOf(false) }
    val isUserSigningOut by viewModel.isUserSigningOut.collectAsStateWithLifecycle()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsStateWithLifecycle()
    val email by viewModel.userEmail.collectAsStateWithLifecycle()
    val phone by viewModel.userPhoneNumber.collectAsStateWithLifecycle()
    val name by viewModel.userName.collectAsStateWithLifecycle()
    val confidentialInfo = listOf(
        stringResource(R.string.name) to name,
        stringResource(R.string.email) to email,
        stringResource(R.string.phone) to phone
    )
    if (isDialogOpen) {
        ProfileDialogScreen(
            modifier = Modifier.width(screenWidthDp - 72.dp),
            isUserSignedIn = isUserSignedIn,
            isUserSigningOut = isUserSigningOut,
            userPhotoUrl = userPhotoUrl,
            onDialogClosed = onDialogClosed,
            onSignOut = { viewModel.signOut() },
            onShowLoginDialog = { showLoginDialog = true },
            confidentialInfo = confidentialInfo
                .filter { !it.second.isNullOrBlank() }
                .map { it.first to it.second!! },
        )
        LoginDialog(
            isDialogOpen = showLoginDialog,
            onDialogClosed = { showLoginDialog = false },
        )
    }
}

@Composable
fun ProfileDialogScreen(
    modifier: Modifier = Modifier,
    isUserSignedIn: Boolean = true,
    isUserSigningOut: Boolean = false,
    userPhotoUrl: Uri? = null,
    confidentialInfo: List<Pair<String, String>> = emptyList(),
    onDialogClosed: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onShowLoginDialog: () -> Unit = {},
    showConfidentialInfoInitially: Boolean = false,
) {
    var showConfidentialInfo by remember { mutableStateOf(showConfidentialInfoInitially) }
    var authenticated by remember { mutableStateOf(false) }
    AlertDialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = modifier,
        onDismissRequest = { onDialogClosed() },
        title = {
            ProfileTitleScreen(
                isUserSignedIn = isUserSignedIn,
                userPhotoUrl = userPhotoUrl,
            )
        },
        confirmButton = {
            Crossfade(targetState = isUserSignedIn) {
                if (it) {
                    TextButton(
                        enabled = !isUserSigningOut,
                        onClick = onSignOut
                    ) {
                        Text(text = stringResource(R.string.sign_out))
                    }
                } else {
                    Button(
                        onClick = onShowLoginDialog
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
                confidentialInfo = confidentialInfo,
                showConfidentialInfo = showConfidentialInfo,
                toggleConfidentialInfoVisibility = {
                    toggleConfidentialInfoVisibility(
                        showConfidentialInfo = showConfidentialInfo,
                        authenticated = authenticated,
//                        fragmentActivity = context as FragmentActivity,
                        onAuthenticationChanged = { authenticated = it },
                        onInfoVisibilityChanged = { showConfidentialInfo = it }
                    )
                }
            )
        }
    )
}

@PreviewLightDarkTheme
@Composable
private fun PreviewProfileDialogScreen(
    name: String = "Illyan",
    email: String = "illyan@google.com",
    phone: String = "+123456789",
) {
    JayTheme {
        Column {
            ProfileDialogScreen(
                modifier = Modifier.width(300.dp),
                userPhotoUrl = null,
                confidentialInfo = listOf(
                    stringResource(R.string.name) to name,
                    stringResource(R.string.email) to email, // I wish one day :)
                    stringResource(R.string.phone) to phone
                ),
                showConfidentialInfoInitially = true
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTitleScreen(
    isUserSignedIn: Boolean = true,
    userPhotoUrl: Uri? = null,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.profile))
        Row(
            modifier = Modifier.weight(1f, fill = true),
            Arrangement.End
        ) {
            AvatarAsyncImage(
                modifier = Modifier
                    .size(RoundedCornerRadius * 3)
                    .clip(CircleShape),
                placeholderEnabled = !isUserSignedIn || userPhotoUrl == null,
                userPhotoUrl = userPhotoUrl
            )
        }
    }
}

@Composable
fun ProfileDetailsScreen(
    confidentialInfo: List<Pair<String, String>> = emptyList(),
    info: List<Pair<String, String>> = emptyList(),
    showConfidentialInfo: Boolean = false,
    toggleConfidentialInfoVisibility: () -> Unit = {},
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val (confidentialInfoText, toggleButton) = createRefs()
        createHorizontalChain(
            confidentialInfoText,
            toggleButton,
            chainStyle = ChainStyle.SpreadInside
        )
        createStartBarrier()
        ConfidentialInfoToggleButton(
            modifier = Modifier
                .constrainAs(toggleButton) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            showConfidentialInfo = showConfidentialInfo,
            anyConfidentialInfo = confidentialInfo.isNotEmpty(),
            toggleConfidentialInfoVisibility = toggleConfidentialInfoVisibility
        )
        LazyRow(
            modifier = Modifier
                .constrainAs(confidentialInfoText) {
                    start.linkTo(parent.start)
                    end.linkTo(toggleButton.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
        ) {
            item {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(confidentialInfo) {
                        UserInfo(
                            infoName = it.first,
                            info = it.second,
                            hide = showConfidentialInfo
                        )
                    }
                    items(info) {
                        UserInfo(
                            infoName = it.first,
                            info = it.second,
                            hide = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfidentialInfoToggleButton(
    modifier: Modifier = Modifier,
    showConfidentialInfo: Boolean = false,
    anyConfidentialInfo: Boolean = false,
    toggleConfidentialInfoVisibility: () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = anyConfidentialInfo
    ) {
        IconButton(
            onClick = toggleConfidentialInfoVisibility
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

@Composable
fun UserInfo(
    infoName: String = stringResource(R.string.unknown),
    info: String = stringResource(R.string.unknown),
    hide: Boolean = true,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "$infoName: ")
        Crossfade(targetState = hide) {
            Text(
                text = if (it) info else stringResource(R.string.hidden_field_string)
            )
        }
    }
}

fun toggleConfidentialInfoVisibility(
    showConfidentialInfo: Boolean,
    authenticated: Boolean,
//    fragmentActivity: FragmentActivity,
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
