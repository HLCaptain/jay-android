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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import illyan.jay.R
import illyan.jay.domain.model.libraries.LicenseType
import illyan.jay.util.findUrlIntervals

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
            LicenseType.Jay_GPLV3 -> JayGPLV3License(
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
    val paragraph = "\n" +
            "\n" +
            "Redistribution and use in source and binary forms, with or without modification," +
            " are permitted provided that the following conditions are met:\n" +
            "\n" +
            "Redistributions of source code must retain the above copyright notice," +
            " this list of conditions and the following disclaimer.\n" +
            "Redistributions in binary form must reproduce the above copyright notice," +
            " this list of conditions and the following disclaimer in the documentation" +
            " and/or other materials provided with the distribution.\n" +
            "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS" +
            " \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO," +
            " THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE" +
            " ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE" +
            " FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL" +
            " DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR" +
            " SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER" +
            " CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY," +
            " OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE" +
            " OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."

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
        style = TextStyle(color = MaterialTheme.colorScheme.onSurface),
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
        style = TextStyle(color = MaterialTheme.colorScheme.onSurface),
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
        style = TextStyle(color = MaterialTheme.colorScheme.onSurface),
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
    val linkStart = title.length + paragraph.indexOf(link)
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
    val paragraph = LicenseType.Jay_GPLV3.description

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