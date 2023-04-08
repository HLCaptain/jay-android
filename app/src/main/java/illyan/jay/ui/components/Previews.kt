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

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Dark theme",
    group = "themes",
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Preview(
    name = "Light theme",
    group = "themes",
    showBackground = true,
)
annotation class PreviewLightDarkTheme

// Jay is not intended to use with foldable phones or desktops (yet)
//@Preview(
//    name = "Foldable screen",
//    device = Devices.FOLDABLE,
//    showBackground = true
//)
//@Preview(
//    name = "Desktop screen",
//    device = Devices.DESKTOP,
//    showBackground = true
//)
@Preview(
    name = "Phone screen",
    group = "screens",
    device = Devices.PHONE,
    showBackground = true
)
@Preview(
    name = "Nexus 5 screen",
    group = "screens",
    device = Devices.NEXUS_5,
    showBackground = true
)
@Preview(
    name = "Pixel 1 screen",
    group = "screens",
    device = Devices.PIXEL,
    showBackground = true
)
@Preview(
    name = "Pixel 4 XL screen",
    group = "screens",
    device = Devices.PIXEL_4_XL,
    showBackground = true
)
annotation class PreviewDeviceScreens

@Preview(
    name = "Large font scale",
    group = "font_scales",
    fontScale = 1.5f,
    showBackground = true
)
@Preview(
    name = "Extra large font scale",
    group = "font_scales",
    fontScale = 2.0f,
    showBackground = true
)
annotation class PreviewFontScales

@PreviewLightDarkTheme
@PreviewDeviceScreens
@PreviewFontScales
annotation class PreviewThemesScreensFonts