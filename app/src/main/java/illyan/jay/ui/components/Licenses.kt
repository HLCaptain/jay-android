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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
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
import illyan.compose.scrollbar.drawVerticalScrollbar
import illyan.jay.R
import illyan.jay.domain.model.libraries.LicenseType
import illyan.jay.ui.theme.statefulColorScheme
import illyan.jay.ui.theme.surfaceColorAtElevation
import illyan.jay.util.findUrlIntervals
import kotlin.math.hypot

@Composable
fun JayTextCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val verticalContentPadding = 6.dp
    val horizontalContentPadding = 6.dp
    val contentPadding = PaddingValues(
        vertical = verticalContentPadding,
        horizontal = horizontalContentPadding
    )
    // Text's corner touches the circle's edge
    val cornerRadius = (hypot(verticalContentPadding.value, horizontalContentPadding.value) * 2).dp
    val lazyListState = rememberLazyListState()
    LazyColumn(
        modifier = modifier
            .drawVerticalScrollbar(lazyListState)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.statefulColorScheme.surfaceColorAtElevation(2.dp)),
        state = lazyListState,
        contentPadding = contentPadding,
    ) {
        item {
            content()
        }
    }
}

@Composable
fun LicenseOfType(
    type: LicenseType? = null,
    authors: List<String> = emptyList(),
    year: Int? = null,
    yearInterval: IntRange? = null,
    beforeTitle: String = "",
    afterTitle: String = "",
) {
    SelectionContainer {
        when (type) {
            LicenseType.ApacheV2 -> ApacheV2License(
                authors = authors,
                year = year,
                yearInterval = yearInterval,
                beforeTitle = beforeTitle,
                afterTitle = afterTitle,
            )
            LicenseType.FreeBSD -> FreeBSDLicense(
                authors = authors,
                year = year,
                yearInterval = yearInterval,
                beforeTitle = beforeTitle,
                afterTitle = afterTitle,
            )
            LicenseType.EclipsePublicLicenseV2 -> EclipsePublicLicense()
            LicenseType.GPLV2 -> GPLV2License(
                authors = authors,
                year = year,
                yearInterval = yearInterval,
                beforeTitle = beforeTitle,
                afterTitle = afterTitle,
            )
            LicenseType.GPLV3 -> GPLV3License(
                authors = authors,
                year = year,
                yearInterval = yearInterval,
                beforeTitle = beforeTitle,
                afterTitle = afterTitle,
            )
            LicenseType.JayGPLV3 -> JayGPLV3License(
                authors = authors,
                year = year,
                yearInterval = yearInterval,
                beforeTitle = beforeTitle,
                afterTitle = afterTitle,
            )
            else -> MissingLicense()
        }
    }
}

@Composable
fun MissingLicense() {
    Text(text = stringResource(R.string.missing_license))
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ApacheV2License(
    authors: List<String> = emptyList(),
    year: Int? = null,
    yearInterval: IntRange? = null,
    beforeTitle: String = "",
    afterTitle: String = "",
) {
    val copyrightYearString = if (yearInterval != null) {
        "${yearInterval.first}-${yearInterval.last}"
    } else year?.toString() ?: ""

    val title = beforeTitle + "Copyright $copyrightYearString ${authors.joinToString(",")}"
        .replace("\\s+".toRegex(), " ") + afterTitle
    val paragraph1 = "\n\nLicensed under the Apache License, Version 2.0 (the \"License\");" +
            " you may not use this file except in compliance with the License." +
            " You may obtain a copy of the License at\n\n\t"
    val link = LicenseType.ApacheV2.url
    val paragraph2 = "\n\nUnless required by applicable law or agreed to in writing, software" +
            " distributed under the License is distributed on an \"AS IS\" BASIS," +
            " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
            " See the License for the specific language governing permissions and" +
            " limitations under the License."

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
                textDecoration = TextDecoration.Underline,
            )
        ) {
            append(link)
        }

        append(paragraph2)

        val whole = title + paragraph1 + link + paragraph2
        val urls = whole.findUrlIntervals()
        urls.forEach {
            addStyle(
                style = SpanStyle(
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline,
                ),
                start = it.first,
                end = it.second,
            )
            addUrlAnnotation(
                urlAnnotation = UrlAnnotation(it.third),
                start = it.first,
                end = it.second
            )
        }
    }

    ClickableText(
        style = TextStyle(color = MaterialTheme.statefulColorScheme.onSurface),
        onClick = { offset ->
            val urlAnnotations = annotatedString.getUrlAnnotations(offset, offset)
            urlAnnotations.firstOrNull()?.let {
                uriHandler.openUri(it.item.url)
            }
        },
        text = annotatedString
    )
}

@Composable
fun FreeBSDLicense(
    authors: List<String> = emptyList(),
    year: Int? = null,
    yearInterval: IntRange? = null,
    beforeTitle: String = "",
    afterTitle: String = "",
) {
    val copyrightYearString = if (yearInterval != null) {
        "${yearInterval.first}-${yearInterval.last}"
    } else year?.toString() ?: ""

    val title = beforeTitle + "Copyright (c) $copyrightYearString ${authors.joinToString(",")}"
        .replace("\\s+".toRegex(), " ") + afterTitle
    val paragraph = LicenseType.FreeBSD.description

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

        // Paragraph
        append(paragraph)
    }

    Text(
        style = TextStyle(color = MaterialTheme.statefulColorScheme.onSurface),
        text = annotatedString
    )
}

@Composable
fun EclipsePublicLicense() {
    val title = LicenseType.EclipsePublicLicenseV2.licenseName + "\n\n"
    val description = LicenseType.EclipsePublicLicenseV2.description

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

        // Paragraph
        append(description)
    }

    Text(
        style = TextStyle(color = MaterialTheme.statefulColorScheme.onSurface),
        text = annotatedString
    )
}

@Composable
fun GPLV2License(
    authors: List<String> = emptyList(),
    year: Int? = null,
    yearInterval: IntRange? = null,
    beforeTitle: String = "",
    afterTitle: String = "",
) {
    val copyrightYearString = if (yearInterval != null) {
        "${yearInterval.first}-${yearInterval.last}"
    } else year?.toString() ?: ""

    val title = beforeTitle + "Copyright (C) $copyrightYearString ${authors.joinToString(",")}"
        .replace("\\s+".toRegex(), " ") + afterTitle
    val paragraph = LicenseType.GPLV2.description

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

        // Paragraph
        append(paragraph)
    }

    Text(
        style = TextStyle(color = MaterialTheme.statefulColorScheme.onSurface),
        text = annotatedString
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun GPLV3License(
    authors: List<String> = emptyList(),
    year: Int? = null,
    yearInterval: IntRange? = null,
    beforeTitle: String = "",
    afterTitle: String = "",
) {
    val copyrightYearString = if (yearInterval != null) {
        "${yearInterval.first}-${yearInterval.last}"
    } else year?.toString() ?: ""

    val title = beforeTitle + "Copyright (C) $copyrightYearString ${authors.joinToString(",")}"
        .replace("\\s+".toRegex(), " ") + afterTitle
    val paragraph = LicenseType.GPLV3.description
    val link = LicenseType.GPLV3.url

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

        // Paragraph
        append(paragraph)

        val whole = title + paragraph
        val urls = whole.findUrlIntervals()
        urls.forEach {
            addStyle(
                style = SpanStyle(
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline,
                ),
                start = it.first,
                end = it.second,
            )
            addUrlAnnotation(
                urlAnnotation = UrlAnnotation(link),
                start = it.first,
                end = it.second
            )
        }
    }

    ClickableText(
        style = TextStyle(color = MaterialTheme.statefulColorScheme.onSurface),
        onClick = { offset ->
            val urlAnnotations = annotatedString.getUrlAnnotations(offset, offset)
            urlAnnotations.firstOrNull()?.let {
                uriHandler.openUri(it.item.url)
            }
        },
        text = annotatedString
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun JayGPLV3License(
    authors: List<String> = emptyList(),
    year: Int? = null,
    yearInterval: IntRange? = null,
    beforeTitle: String = "",
    afterTitle: String = "",
) {
    val copyrightYearString = if (yearInterval != null) {
        "${yearInterval.first}-${yearInterval.last}"
    } else year?.toString() ?: ""

    val title = beforeTitle + "Copyright (C) $copyrightYearString ${authors.joinToString(",")}"
        .replace("\\s+".toRegex(), " ") + afterTitle
    val paragraph = LicenseType.JayGPLV3.description

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

        // Paragraph
        append(paragraph)

        val whole = title + paragraph
        val urls = whole.findUrlIntervals()
        urls.forEach {
            addStyle(
                style = SpanStyle(
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline,
                ),
                start = it.first,
                end = it.second,
            )
            addUrlAnnotation(
                urlAnnotation = UrlAnnotation(it.third),
                start = it.first,
                end = it.second
            )
        }
    }

    Column {
        ClickableText(
            style = TextStyle(color = MaterialTheme.statefulColorScheme.onSurface),
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