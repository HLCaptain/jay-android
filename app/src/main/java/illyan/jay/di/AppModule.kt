/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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

import android.content.Context
import android.hardware.SensorManager
import androidx.core.graphics.drawable.IconCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.SearchEngineSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import illyan.jay.BuildConfig
import illyan.jay.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideAppContext(@ApplicationContext context: Context) = context

    @Provides
    fun provideFirebaseAuth() = Firebase.auth

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun provideIcon(@ApplicationContext context: Context) =
        IconCompat.createWithResource(context, R.drawable.ic_jay_marker_icon_v3_round)

    @Provides
    @Singleton
    fun provideLocalBroadcastManager(@ApplicationContext context: Context) =
        LocalBroadcastManager.getInstance(context)

    @Provides
    @Singleton
    fun provideSearchEngine() =
        MapboxSearchSdk.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings(accessToken = BuildConfig.MapboxSdkRegistryToken)
        )

    @Provides
    @Singleton
    fun provideHistoryDataProvider() =
        MapboxSearchSdk.serviceProvider.historyDataProvider()

    @Provides
    @Singleton
    fun provideFavoritesDataProvider() =
        MapboxSearchSdk.serviceProvider.favoritesDataProvider()

    @Provides
    @CoroutineScopeIO
    fun provideCoroutineScopeIO() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @CoroutineScopeMain
    fun provideCoroutineScopeMain() = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}


