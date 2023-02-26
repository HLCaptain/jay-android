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

package illyan.jay.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import illyan.jay.R
import illyan.jay.ui.theme.JayTheme

@Composable
fun AvatarAsyncImage(
    modifier: Modifier = Modifier,
    placeholderEnabled: Boolean = false,
    userPhotoUrl: Uri?,
    placeholder: @Composable () -> Unit = { DefaultAvatar() },
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (!placeholderEnabled) {
            SubcomposeAsyncImage(
                modifier = modifier,
                model = userPhotoUrl?.toString(),
                loading = {
                    when (painter.state) {
                        is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                        is AsyncImagePainter.State.Loading -> PlaceholderAvatar(modifier)
                        is AsyncImagePainter.State.Error -> BrokenAvatar(modifier)
                        else -> DefaultAvatar(modifier)
                    }
                },
                contentDescription = stringResource(R.string.avatar_profile_picture)
            )
        } else placeholder()
    }
}

@Composable
fun DefaultAvatar(
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        Image(
            modifier = modifier,
            painter = painterResource(id = R.drawable.ic_illyan_avatar_color),
            contentDescription = stringResource(R.string.avatar_profile_picture),
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            modifier = modifier,
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = stringResource(R.string.avatar_profile_picture)
        )
    }
}

@PreviewLightDarkTheme
@Composable
private fun DefaultAvatarPreview() {
    JayTheme {
        DefaultAvatar()
    }
}

@Composable
fun BrokenAvatar(
    modifier: Modifier = Modifier
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Rounded.BrokenImage,
        contentDescription = stringResource(R.string.avatar_profile_picture)
    )
}

@PreviewLightDarkTheme
@Composable
private fun BrokenAvatarPreview() {
    JayTheme {
        BrokenAvatar()
    }
}

@Composable
fun PlaceholderAvatar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .placeholder(
                visible = true,
                highlight = PlaceholderHighlight.shimmer()
            )
    )
}

@PreviewLightDarkTheme
@Composable
private fun PlaceholderAvatarPreview() {
    JayTheme {
        PlaceholderAvatar()
    }
}
