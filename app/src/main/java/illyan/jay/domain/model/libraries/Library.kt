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

package illyan.jay.domain.model.libraries

import android.os.Parcelable
import illyan.jay.data.serializers.YearIntervalParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.time.ZonedDateTime

@Parcelize
@TypeParceler<IntRange?, YearIntervalParceler>
data class Library(
    val name: String = "",
    val license: License? = null,
    val repositoryUrl: String? = null,
    val moreInfoUrl: String? = null,
    val authors: List<String> = emptyList(),
    val privacyPolicyUrl: String? = null,
    val termsAndConditionsUrl: String? = null,
) : Parcelable {
    companion object {
        val ComposeScrollbarLibrary = Library(
            name = "Compose Scrollbar",
            license = License(
                copyrightOwners = listOf("Balázs Püspök-Kiss (Illyan)"),
                year = 2023,
                type = LicenseType.ApacheV2,
            ),
            repositoryUrl = "https://github.com/HLCaptain/compose-scrollbar",
            moreInfoUrl = "https://github.com/HLCaptain/compose-scrollbar",
            authors = listOf("Balázs Püspök-Kiss (Illyan)"),
        )
        val PlumberLibrary = Library(
            name = "Plumber",
            license = License(
                copyrightOwners = listOf("Balázs Püspök-Kiss (Illyan)"),
                year = 2023,
                type = LicenseType.ApacheV2,
            ),
            repositoryUrl = "https://github.com/HLCaptain/plumber",
            authors = listOf("Balázs Püspök-Kiss (Illyan)"),
        )
        val SwipeLibrary = Library(
            name = "swipe",
            license = License(
                copyrightOwners = listOf("Saket Narayan"),
                year = 2022,
                type = LicenseType.ApacheV2,
                url = "https://github.com/saket/swipe/blob/trunk/LICENSE.txt",
            ),
            repositoryUrl = "https://github.com/saket/swipe",
            authors = listOf("Saket Narayan"),
        )
        val AccompanistLibrary = Library(
            name = "Accompanist",
            license = License(
                copyrightOwners = listOf("The Android Open Source Project"),
                year = 2020,
                type = LicenseType.ApacheV2,
                url = "https://github.com/google/accompanist/blob/main/LICENSE",
            ),
            repositoryUrl = "https://github.com/google/accompanist",
            moreInfoUrl = "https://google.github.io/accompanist/",
            authors = listOf("The Android Open Source Project"),
        )
        val HiltLibrary = Library(
            name = "Dagger Hilt",
            license = License(
                copyrightOwners = listOf("The Dagger Authors"),
                year = 2012,
                type = LicenseType.ApacheV2,
                url = "https://github.com/google/dagger/blob/master/LICENSE.txt",
            ),
            repositoryUrl = "https://github.com/google/dagger",
            moreInfoUrl = "https://dagger.dev/hilt/",
            authors = listOf("Google"),
        )
        val TimberLibrary = Library(
            name = "Timber",
            license = License(
                copyrightOwners = listOf("Jake Wharton"),
                year = 2013,
                type = LicenseType.ApacheV2,
                url = "https://github.com/JakeWharton/timber/blob/trunk/LICENSE.txt",
            ),
            repositoryUrl = "https://github.com/JakeWharton/timber",
            moreInfoUrl = "https://jakewharton.github.io/timber/docs/5.x/",
            authors = listOf("Jake Wharton"),
        )
        val ComposeDestinationsLibrary = Library(
            name = "Compose Destinations",
            license = License(
                copyrightOwners = listOf("Rafael Costa"),
                year = 2021,
                type = LicenseType.ApacheV2,
                url = "https://github.com/raamcosta/compose-destinations/blob/main/LICENSE.txt",
            ),
            repositoryUrl = "https://github.com/raamcosta/compose-destinations",
            moreInfoUrl = "https://composedestinations.rafaelcosta.xyz/",
            authors = listOf("Rafael Costa"),
        )
        val CoilLibrary = Library(
            name = "Coil",
            license = License(
                copyrightOwners = listOf("Coil Contributors"),
                year = 2021,
                type = LicenseType.ApacheV2,
                url = "https://github.com/coil-kt/coil/blob/main/LICENSE.txt",
            ),
            repositoryUrl = "https://github.com/coil-kt/coil",
            moreInfoUrl = "https://coil-kt.github.io/coil/",
            authors = listOf(
                "John Carlson",
                "Colin White",
                "Coil Contributors"
            ),
        )
        val RoomLibrary = Library(
            name = "Room",
            license = License(
                copyrightOwners = listOf("The Android Open Source Project"),
                year = 2023,
                type = LicenseType.ApacheV2,
            ),
            moreInfoUrl = "https://developer.android.com/training/data-storage/room",
            authors = listOf("The Android Open Source Project"),
        )
        val DataStoreLibrary = Library(
            name = "DataStore",
            license = License(
                copyrightOwners = listOf("The Android Open Source Project"),
                year = 2022,
                type = LicenseType.ApacheV2,
            ),
            moreInfoUrl = "https://developer.android.com/topic/libraries/architecture/datastore",
            authors = listOf("The Android Open Source Project"),
        )
        val KotlinSerializationLibrary = Library(
            name = "Kotlin Serialization",
            license = License(
                copyrightOwners = listOf("Kotlin Foundation"),
                year = 2023,
                type = LicenseType.ApacheV2,
                url = "https://github.com/Kotlin/kotlinx.serialization/blob/master/LICENSE.txt",
            ),
            moreInfoUrl = "https://kotlinlang.org/docs/serialization.html",
            repositoryUrl = "https://github.com/Kotlin/kotlinx.serialization",
            authors = listOf("Kotlin Foundation"),
        )
        val KotlinImmutableCollectionsLibrary = Library(
            name = "Kotlin Immutable Collections",
            license = License(
                copyrightOwners = listOf("Kotlin Foundation"),
                year = 2021,
                type = LicenseType.ApacheV2,
            ),
            repositoryUrl = "https://github.com/Kotlin/kotlinx.collections.immutable",
            authors = listOf("Kotlin Foundation"),
        )
        val KotlinCoroutinesLibrary = Library(
            name = "Kotlin Coroutines",
            license = License(
                copyrightOwners = listOf("Kotlin Foundation"),
                year = 2023,
                type = LicenseType.ApacheV2,
                url = "https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/LICENSE.txt",
            ),
            moreInfoUrl = "https://kotlinlang.org/docs/coroutines-overview.html",
            repositoryUrl = "https://github.com/Kotlin/kotlinx.coroutines",
            authors = listOf("Kotlin Foundation"),
        )
        val ZstdJniLibrary = Library(
            name = "Zstd-jni",
            license = License(
                copyrightOwners = listOf("Luben Karavelov"),
                yearInterval = IntRange(2015, ZonedDateTime.now().year),
                type = LicenseType.FreeBSD,
                beforeTitle = "Zstd-jni: JNI bindings to Zstd Library\n\n",
                afterTitle = " All rights reserved.\n\nBSD License",
                url = "https://github.com/luben/zstd-jni/blob/master/LICENSE",
            ),
            repositoryUrl = "https://github.com/luben/zstd-jni",
            authors = listOf("Luben Karavelov"),
        )
        val GoogleMapsUtilitiesLibrary = Library(
            name = "Maps SDK for Android Utility Library",
            license = License(
                copyrightOwners = listOf("Google"),
                year = ZonedDateTime.now().year,
                type = LicenseType.ApacheV2,
                url = "https://github.com/googlemaps/android-maps-utils/blob/main/LICENSE",
            ),
            repositoryUrl = "https://github.com/googlemaps/android-maps-utils",
            moreInfoUrl = "https://developers.google.com/maps/documentation/android-sdk/utility",
            authors = listOf("Google"),
        )
        val GooglePlayServicesLibrary = Library(
            name = "Google Play Services",
            license = License(
                copyrightOwners = listOf("Google"),
                year = ZonedDateTime.now().year,
                type = LicenseType.ApacheV2,
            ),
            moreInfoUrl = "https://developers.google.com/android/guides/setup",
            authors = listOf("Google"),
        )
        val FirebaseAndroidSDKLibrary = Library(
            name = "Firebase Android SDK",
            license = License(
                copyrightOwners = listOf("Google"),
                year = ZonedDateTime.now().year,
                type = LicenseType.ApacheV2,
                url = "https://github.com/firebase/firebase-android-sdk/blob/master/LICENSE",
            ),
            moreInfoUrl = "https://firebase.google.com/",
            repositoryUrl = "https://github.com/firebase/firebase-android-sdk",
            authors = listOf("Google"),
        )
        val JUnit5Library = Library(
            name = "JUnit 5",
            license = License(
                copyrightOwners = listOf("The JUnit Team"),
                type = LicenseType.EclipsePublicLicenseV2,
                url = "https://github.com/junit-team/junit5/blob/main/LICENSE.md",
            ),
            moreInfoUrl = "https://junit.org/junit5/",
            repositoryUrl = "https://github.com/junit-team/junit5",
            authors = listOf("The JUnit Team"),
        )
        val MockKLibrary = Library(
            name = "MockK",
            license = License(
                copyrightOwners = listOf("github.com/mockk"),
                year = 2017,
                type = LicenseType.ApacheV2,
                url = "https://github.com/mockk/mockk/blob/master/LICENSE",
            ),
            moreInfoUrl = "https://mockk.io/",
            repositoryUrl = "https://github.com/mockk/mockk",
            authors = listOf("github.com/mockk"),
        )
        val GradleSecretsLibrary = Library(
            name = "Secrets Gradle Plugin for Android",
            license = License(
                copyrightOwners = listOf("Google"),
                year = ZonedDateTime.now().year,
                type = LicenseType.ApacheV2,
                url = "https://github.com/google/secrets-gradle-plugin/blob/main/LICENSE",
            ),
            moreInfoUrl = "https://developers.google.com/maps/documentation/android-sdk/secrets-gradle-plugin",
            repositoryUrl = "https://github.com/google/secrets-gradle-plugin",
            authors = listOf("Google"),
        )
        val Jay = Library(
            name = "Jay",
            license = License(
                copyrightOwners = listOf("Balázs Püspök-Kiss (Illyan)"),
                type = LicenseType.JayGPLV3,
                url = "https://github.com/HLCaptain/jay-android/blob/main/LICENSE",
            ),
            repositoryUrl = "https://github.com/HLCaptain/jay-android",
            authors = listOf("Balázs Püspök-Kiss (Illyan)"),
        )
    }
}
