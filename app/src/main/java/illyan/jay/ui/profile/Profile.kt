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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.startDestination
import illyan.jay.MainActivity
import illyan.jay.R
import illyan.jay.ui.NavGraphs
import illyan.jay.ui.components.AvatarAsyncImage
import illyan.jay.ui.components.CopiedToKeyboardTooltip
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.JayDialogSurface
import illyan.jay.ui.components.MenuButton
import illyan.jay.ui.components.PreviewAccessibility
import illyan.jay.ui.components.TooltipElevatedCard
import illyan.jay.ui.components.dialogWidth
import illyan.jay.ui.destinations.AboutDialogScreenDestination
import illyan.jay.ui.destinations.LoginDialogScreenDestination
import illyan.jay.ui.destinations.UserSettingsDialogScreenDestination
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.theme.JayTheme
import java.util.UUID

@RootNavGraph
@NavGraph
annotation class ProfileNavGraph(
    val start: Boolean = false,
)

val LocalDialogDismissRequest = compositionLocalOf { {} }
val LocalDialogActivityProvider = compositionLocalOf<MainActivity?> { null }

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class, ExperimentalAnimationApi::class
)
@Composable
fun ProfileDialog(
    isDialogOpen: Boolean = true,
    onDialogClosed: () -> Unit = {},
) {
    if (isDialogOpen) {
        val context = LocalContext.current
        // Don't use exit animations because
        // it looks choppy while Dialog resizes due to content change.
        val engine = rememberAnimatedNavHostEngine(
            rootDefaultAnimations = RootNavGraphDefaultAnimations(
                enterTransition = {
                    slideInHorizontally(tween(200)) { it / 8 } + fadeIn(tween(200))
                },
                popEnterTransition = {
                    slideInHorizontally(tween(200)) { -it / 8 } + fadeIn(tween(200))
                }
            )
        )
        val navController = engine.rememberNavController()
        val currentDestination by navController.currentDestinationAsState()
        val onDismissRequest: () -> Unit = {
            if (currentDestination == NavGraphs.profile.startDestination) {
                onDialogClosed()
            } else {
                navController.navigateUp()
            }
        }
        AlertDialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            onDismissRequest = onDismissRequest,
        ) {
            JayDialogContent(
                surface = { JayDialogSurface(content = it) },
            ) {
                CompositionLocalProvider(
                    LocalDialogDismissRequest provides onDismissRequest,
                    LocalDialogActivityProvider provides context as MainActivity
                ) {
                    DestinationsNavHost(
                        modifier = Modifier.fillMaxWidth(),
                        navGraph = NavGraphs.profile,
                        engine = engine,
                        navController = navController,
                    )
                }
            }
        }
    }
}

@ProfileNavGraph(start = true)
@Destination
@Composable
fun ProfileDialogScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val userUUID by viewModel.userUUID.collectAsStateWithLifecycle()
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsStateWithLifecycle()
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
    ProfileDialogContent(
        userUUID = userUUID,
        isUserSignedIn = isUserSignedIn,
        isUserSigningOut = isUserSigningOut,
        userPhotoUrl = userPhotoUrl,
        confidentialInfo = confidentialInfo,
        showConfidentialInfoInitially = false,
        onSignOut = viewModel::signOut,
        onShowLoginScreen = { destinationsNavigator.navigate(LoginDialogScreenDestination) },
        onShowAboutScreen = { destinationsNavigator.navigate(AboutDialogScreenDestination) },
        onShowSettingsScreen = { destinationsNavigator.navigate(UserSettingsDialogScreenDestination) }
    )
}

@Composable
fun ProfileDialogContent(
    modifier: Modifier = Modifier,
    userUUID: String? = null,
    isUserSignedIn: Boolean = true,
    isUserSigningOut: Boolean = false,
    userPhotoUrl: Uri? = null,
    confidentialInfo: List<Pair<String, String?>> = emptyList(),
    showConfidentialInfoInitially: Boolean = false,
    onSignOut: () -> Unit = {},
    onShowLoginScreen: () -> Unit = {},
    onShowAboutScreen: () -> Unit = {},
    onShowSettingsScreen: () -> Unit = {}
) {
    var showConfidentialInfo by remember { mutableStateOf(showConfidentialInfoInitially) }
    JayDialogContent(
        modifier = modifier,
        title = {
            ProfileTitleScreen(
                userUUID = userUUID,
                isUserSignedIn = isUserSignedIn,
                userPhotoUrl = userPhotoUrl,
                showConfidentialInfo = showConfidentialInfo
            )
        },
        text = {
            ProfileScreen(
                confidentialInfo = confidentialInfo
                    .filter { !it.second.isNullOrBlank() }
                    .map { it.first to it.second!! },
                onVisibilityChanged = { showConfidentialInfo = it },
                showConfidentialInfo = showConfidentialInfo
            )
        },
        buttons = {
            ProfileButtons(
                isUserSignedIn = isUserSignedIn,
                isUserSigningOut = isUserSigningOut,
                onLogin = onShowLoginScreen,
                onSignOut = onSignOut,
                onShowAboutScreen = onShowAboutScreen,
                onShowSettingsScreen = onShowSettingsScreen,
            )
        },
        containerColor = Color.Transparent,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileButtons(
    onShowSettingsScreen: () -> Unit = {},
    onShowAboutScreen: () -> Unit = {},
    onLogin: () -> Unit = {},
    onSignOut: () -> Unit = {},
    isUserSignedIn: Boolean = false,
    isUserSigningOut: Boolean = false,
) {
    val onDialogClosed = LocalDialogDismissRequest.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ProfileMenu(
            onShowSettingsScreen = onShowSettingsScreen,
            onShowAboutScreen = onShowAboutScreen,
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Bottom),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onDialogClosed() }) {
                Text(text = stringResource(R.string.close))
            }
            // FIXME: find out why Crossfade does not work here
            if (isUserSignedIn) {
                TextButton(
                    enabled = !isUserSigningOut,
                    onClick = onSignOut,
                ) {
                    Text(text = stringResource(R.string.sign_out))
                }
            } else {
                Button(
                    onClick = onLogin
                ) {
                    Text(text = stringResource(R.string.sign_in))
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    showConfidentialInfo: Boolean = false,
    confidentialInfo: List<Pair<String, String>> = emptyList(),
    onVisibilityChanged: (Boolean) -> Unit = {}
) {
    var authenticated by remember { mutableStateOf(false) }
    ProfileDetailsScreen(
        modifier = modifier,
        confidentialInfo = confidentialInfo,
        showConfidentialInfo = showConfidentialInfo,
        onConfidentialInfoVisibilityChanged = {
            toggleConfidentialInfoVisibility(
                showConfidentialInfo = showConfidentialInfo,
                authenticated = authenticated,
//                        fragmentActivity = context as FragmentActivity,
                onAuthenticationChanged = { authenticated = it },
                onVisibilityChanged = onVisibilityChanged
            )
        }
    )
}

@PreviewAccessibility
@Composable
private fun PreviewProfileDialogScreen(
    name: String = "Illyan",
    email: String = "illyan@google.com",
    phone: String = "+123456789",
) {
    JayTheme {
        Column {
            JayDialogSurface {
                ProfileDialogContent(
                    modifier = Modifier.dialogWidth(),
                    userUUID = UUID.randomUUID().toString(),
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTitleScreen(
    modifier: Modifier = Modifier,
    userUUID: String? = null,
    isUserSignedIn: Boolean = true,
    showConfidentialInfo: Boolean = false,
    userPhotoUrl: Uri? = null,
) {
    val clipboard = LocalClipboardManager.current
    FlowRow(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Text(text = stringResource(R.string.profile))
            AnimatedVisibility(visible = userUUID != null) {
                if (userUUID != null) {
                    TooltipElevatedCard(
                        tooltip = { CopiedToKeyboardTooltip() },
                        disabledTooltip = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = stringResource(R.string.locked),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        enabled = showConfidentialInfo,
                        showTooltipOnClick = true,
                        onShowTooltip = {
                            if (showConfidentialInfo) {
                                clipboard.setText(AnnotatedString(text = userUUID))
                            }
                        }
                    ) {
                        UserInfo(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            infoName = stringResource(id = R.string.user_id),
                            info = userUUID.take(8),
                            show = showConfidentialInfo,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.weight(1f),
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
    modifier: Modifier = Modifier,
    confidentialInfo: List<Pair<String, String>> = emptyList(),
    info: List<Pair<String, String>> = emptyList(),
    showConfidentialInfo: Boolean = false,
    onConfidentialInfoVisibilityChanged: (Boolean) -> Unit = {},
) {
    Column(
        modifier = modifier
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
                onVisibilityChanged = onConfidentialInfoVisibilityChanged
            )
            UserInfoList(
                modifier = Modifier
                    .constrainAs(confidentialInfoText) {
                        start.linkTo(parent.start)
                        end.linkTo(toggleButton.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
                confidentialInfo = confidentialInfo,
                info = info,
                showConfidentialInfo = showConfidentialInfo
            )
        }
    }
}

@Composable
fun UserInfoList(
    modifier: Modifier = Modifier,
    confidentialInfo: List<Pair<String, String>> = emptyList(),
    info: List<Pair<String, String>> = emptyList(),
    showConfidentialInfo: Boolean = false,
) {
    LazyRow(
        modifier = modifier,
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
                        show = showConfidentialInfo
                    )
                }
                items(info) {
                    UserInfo(
                        infoName = it.first,
                        info = it.second,
                        show = true
                    )
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
    onVisibilityChanged: (Boolean) -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = anyConfidentialInfo
    ) {
        IconToggleButton(
            checked = showConfidentialInfo,
            onCheckedChange = onVisibilityChanged
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
    modifier: Modifier = Modifier,
    infoName: String = stringResource(R.string.unknown),
    info: String = stringResource(R.string.unknown),
    show: Boolean = false,
    style: TextStyle = LocalTextStyle.current,
    nameStyle: TextStyle = style.plus(TextStyle(fontWeight = FontWeight.SemiBold)),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = infoName,
            style = nameStyle
        )
        Crossfade(
            targetState = show,
            label = "User info"
        ) {
            Text(
                text = if (it) info else stringResource(R.string.hidden_field_string),
                style = style
            )
        }
    }
}

@Composable
fun ProfileMenu(
    modifier: Modifier = Modifier,
    onShowAboutScreen: () -> Unit = {},
    onShowSettingsScreen: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy((-12).dp)
    ) {
        MenuButton(
            onClick = onShowAboutScreen,
            text = stringResource(R.string.about)
        )
        MenuButton(
            onClick = onShowSettingsScreen,
            text = stringResource(R.string.settings)
        )
    }
}

fun toggleConfidentialInfoVisibility(
    showConfidentialInfo: Boolean,
    authenticated: Boolean,
//    fragmentActivity: FragmentActivity,
    onAuthenticationChanged: (Boolean) -> Unit,
    onVisibilityChanged: (Boolean) -> Unit,
) {
    if (showConfidentialInfo) {
        onVisibilityChanged(false)
    } else {
        if (authenticated) {
            onVisibilityChanged(true)
        } else {
            onVisibilityChanged(true)
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
