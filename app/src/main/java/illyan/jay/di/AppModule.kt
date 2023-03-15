/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import illyan.jay.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideAppContext(@ApplicationContext context: Context) = context

    @Singleton
    @Provides
    fun provideAppLifecycle() = ProcessLifecycleOwner.get().lifecycle

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
        IconCompat.createWithResource(context, R.drawable.jay_marker_icon_v3_round)

    @Provides
    @Singleton
    fun provideLocalBroadcastManager(@ApplicationContext context: Context) =
        LocalBroadcastManager.getInstance(context)

    @Provides
    @CoroutineScopeIO
    fun provideCoroutineScopeIO() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @CoroutineScopeMain
    fun provideCoroutineScopeMain() = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Provides
    @CoroutineDispatcherIO
    fun provideCoroutineDispatcherIO() = Dispatchers.IO

    @Provides
    @CoroutineDispatcherMain
    fun provideCoroutineDispatcherMain() = Dispatchers.Main

    @Provides
    @Singleton
    fun provideCrashlytics() = Firebase.crashlytics

    @Provides
    fun provideDebugTree() = Timber.DebugTree()

    @Provides
    @Singleton
    fun provideAnalytics() = Firebase.analytics

    @Provides
    @Singleton
    fun providePerformance() = Firebase.performance
}
