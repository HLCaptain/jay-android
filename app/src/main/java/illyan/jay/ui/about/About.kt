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

package illyan.jay.ui.about

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.BuildConfig
import illyan.jay.R
import illyan.jay.domain.model.libraries.Library
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.destinations.LibrariesDialogScreenDestination
import illyan.jay.ui.profile.MenuButton
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.settings.general.ShowAdsSetting
import illyan.jay.ui.theme.JayTheme
import illyan.jay.ui.theme.signaturePink
import illyan.jay.util.TestAdUnitIds

@ProfileNavGraph
@Destination
@Composable
fun AboutDialogScreen(
    viewModel: AboutViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val showAds by viewModel.showAds.collectAsStateWithLifecycle()
    AboutDialogContent(
        modifier = Modifier.fillMaxWidth(),
        isShowingAd = showAds,
        aboutBannerAdUnitId = viewModel.aboutBannerAdUnitId,
        setAdVisibility = viewModel::setAdVisibility,
        onNavigateToLibraries = {
            destinationsNavigator.navigate(LibrariesDialogScreenDestination)
        }
    )
}

@Composable
fun AboutDialogContent(
    modifier: Modifier = Modifier,
    isShowingAd: Boolean = false,
    aboutBannerAdUnitId: String = TestAdUnitIds.Banner,
    setAdVisibility: (Boolean) -> Unit = {},
    onNavigateToLibraries: () -> Unit = {},
) {
    JayDialogContent(
        modifier = modifier,
        title = { AboutTitle() },
        text = {
            AboutScreen(
                isShowingAd = isShowingAd,
                setAdVisibility = setAdVisibility,
                onNavigateToLibraries = onNavigateToLibraries,
                aboutBannerAdUnitId = aboutBannerAdUnitId,
            )
        },
        buttons = {
            AboutButtons()
        },
        containerColor = Color.Transparent,
    )
}

@Composable
fun AboutTitle() {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(R.string.about))
            Text(text = stringResource(R.string.app_name))
        }
        Text(
            text = stringResource(R.string.app_description_brief),
            style = MaterialTheme.typography.bodySmall,
            color = AlertDialogDefaults.textContentColor
        )
    }
}

@Composable
fun AboutScreen(
    isShowingAd: Boolean = false,
    aboutBannerAdUnitId: String = TestAdUnitIds.Banner,
    setAdVisibility: (Boolean) -> Unit = {},
    onNavigateToLibraries: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    Column {
        Column(
            verticalArrangement = Arrangement.spacedBy((-12).dp)
        ) {
            MenuButton(
                text = stringResource(R.string.libraries),
                onClick = onNavigateToLibraries
            )
            AnimatedVisibility(visible = Library.Jay.license?.url != null) {
                MenuButton(
                    text = stringResource(R.string.jay_license),
                    onClick = { Library.Jay.license?.url?.let { uriHandler.openUri(it) } }
                )
            }
            AnimatedVisibility(visible = Library.Jay.privacyPolicyUrl != null) {
                MenuButton(
                    text = stringResource(R.string.privacy_policy),
                    onClick = { Library.Jay.privacyPolicyUrl?.let { uriHandler.openUri(it) } }
                )
            }
            AnimatedVisibility(visible = Library.Jay.termsAndConditionsUrl != null) {
                MenuButton(
                    text = stringResource(R.string.terms_and_conditions),
                    onClick = { Library.Jay.termsAndConditionsUrl?.let { uriHandler.openUri(it) } }
                )
            }
        }
        // TODO: show main developers
        // Place new sections here
        DonationScreen(
            isShowingAd = isShowingAd,
            setAdVisibility = setAdVisibility,
            aboutBannerAdUnitId = aboutBannerAdUnitId
        )
    }
}

@Composable
fun DonationScreen(
    modifier: Modifier = Modifier,
    isShowingAd: Boolean = false,
    setAdVisibility: (Boolean) -> Unit = {},
    aboutBannerAdUnitId: String = TestAdUnitIds.Banner,
) {
    Column(
        modifier = modifier
    ) {
        AboutAdSetting(
            modifier = Modifier.fillMaxWidth(),
            isShowingAd = isShowingAd,
            setAdVisibility = setAdVisibility,
        )
        AboutAdScreen(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            isShowingAd = isShowingAd,
            adUnitId = aboutBannerAdUnitId
        )
    }
}

@Composable
fun AboutButtons() {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                text = "${BuildConfig.VERSION_NAME} v${BuildConfig.VERSION_CODE}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.signaturePink),
            onClick = { uriHandler.openUri("https://ko-fi.com/illyan") }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Rounded.Favorite, contentDescription = "")
                Text(text = stringResource(R.string.support_jay))
            }
        }
    }
}

@Composable
fun AboutAdSetting(
    modifier: Modifier = Modifier,
    isShowingAd: Boolean = false,
    setAdVisibility: (Boolean) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ShowAdsSetting(
            showAds = isShowingAd,
            setAdVisibility = setAdVisibility
        )
        AnimatedVisibility(visible = !isShowingAd) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Rounded.VolunteerActivism, contentDescription = "")
                    Text(text = stringResource(id = R.string.support_jay_with_ad_description))
                }
            }
        }
    }
}


@Composable
fun AboutAdScreen(
    modifier: Modifier = Modifier,
    isShowingAd: Boolean = false,
    adUnitId: String = TestAdUnitIds.Banner,
) {
    AnimatedVisibility(
        modifier = modifier
            .padding(6.dp)
            .clip(RoundedCornerShape(6.dp)),
        visible = isShowingAd
    ) {
        AndroidView(
            modifier = Modifier.heightIn(min = AdSize.BANNER.height.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@PreviewLightDarkTheme
@Composable
private fun AboutDialogScreenPreview() {
    JayTheme {
        JayDialogContent {
            AboutDialogContent(modifier = Modifier.fillMaxWidth())
        }
    }
}
