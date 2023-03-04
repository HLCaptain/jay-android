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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import illyan.jay.domain.model.libraries.LicenseType

@Composable
fun getDefaultLicenseOf(
    type: LicenseType? = null,
    authors: List<String> = emptyList(),
    year: Int? = null,
    yearInterval: IntRange? = null,
) {
    SelectionContainer {
        when (type) {
            LicenseType.ApacheV2 -> ApacheV2License(
                authors = authors,
                year = year,
                yearInterval = yearInterval
            )
            else -> MissingLicense()
        }
    }
}

@Composable
fun MissingLicense() {
    Text(text = "Missing license")
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ApacheV2License(
    authors: List<String> = emptyList(),
    year: Int? = null,
    yearInterval: IntRange? = null,
) {
    val copyrightYearString = if (yearInterval != null) {
        "${yearInterval.first}-${yearInterval.last}"
    } else year?.toString() ?: ""

    val title = "Copyright $copyrightYearString ${authors.joinToString(",")}"
        .replace("\\s+".toRegex(), " ") + ""
    val paragraph1 = "\n\nLicensed under the Apache License, Version 2.0 (the \"License\");" +
            " you may not use this file except in compliance with the License." +
            " You may obtain a copy of the License at\n\n\t"
    val link = LicenseType.ApacheV2.url
    val paragraph2 = "\n\nUnless required by applicable law or agreed to in writing, software" +
            " distributed under the License is distributed on an \"AS IS\" BASIS," +
            " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
            " See the License for the specific language governing permissions and" +
            " limitations under the License."
    val linkStart = (title + paragraph1).length
    val linkEnd = linkStart + link.length

    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        // Title
        withStyle(
            style = SpanStyle(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
            )
        ) {
            append(title)
        }

        // Paragraph 1
        append(paragraph1)

        // Link
        withStyle(
            style = SpanStyle(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
            )
        ) {
            append(LicenseType.ApacheV2.url)
        }

        append(paragraph2)


        addUrlAnnotation(
            urlAnnotation = UrlAnnotation(LicenseType.ApacheV2.url),
            start = linkStart,
            end = linkEnd
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ClickableText(
            style = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            onClick = { offset ->
                val urlAnnotations = annotatedString.getUrlAnnotations(offset, offset)
                urlAnnotations.firstOrNull()?.let {
                    uriHandler.openUri(it.item.url)
                }
            },
            text = annotatedString
        )
    }
}