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

package illyan.jay.di

import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.ServiceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import illyan.jay.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapboxModule {

    @Provides
    @Singleton
    fun provideSearchEngine() =
        SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings(accessToken = BuildConfig.MapboxSdkRegistryToken)
        )

    @Provides
    @Singleton
    fun provideHistoryDataProvider() = ServiceProvider.INSTANCE.historyDataProvider()

    @Provides
    @Singleton
    fun provideFavoritesDataProvider() = ServiceProvider.INSTANCE.favoritesDataProvider()
}