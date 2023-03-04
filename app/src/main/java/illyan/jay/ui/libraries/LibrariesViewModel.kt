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

package illyan.jay.ui.libraries

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.model.libraries.Library
import illyan.jay.ui.libraries.model.UiLibrary
import illyan.jay.ui.libraries.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LibrariesViewModel @Inject constructor(

) : ViewModel() {
    private val _libraries = MutableStateFlow<List<UiLibrary>>(emptyList())
    val libraries = _libraries.asStateFlow()

    init {
        _libraries.value = Libraries
    }

    companion object {
        val Libraries = Library.run {
            listOf(
                ComposeScrollbarLibrary,
                PlumberLibrary,
                SwipeLibrary,
                AccompanistLibrary,
                HiltLibrary,
                TimberLibrary,
                ComposeDestinationsLibrary,
                CoilLibrary,
                RoomLibrary,
                DataStoreLibrary,
                KotlinSerializationLibrary,
                KotlinImmutableCollectionsLibrary,
                KotlinCoroutinesLibrary,
                ZstdJniLibrary,
                GoogleMapsUtilitiesLibrary,
                GooglePlayServicesLibrary,
                FirebaseAndroidSDKLibrary,
                JUnit5Library,
                MockKLibrary
            ).map { it.toUiModel() }.sortedBy { it.name.lowercase() }
        }
    }
}