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

package illyan.jay

import android.app.Application
import androidx.datastore.core.DataStore
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import illyan.jay.data.disk.model.AppSettings
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import illyan.jay.util.log.CrashlyticsTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject lateinit var crashlyticsTree: CrashlyticsTree
    @Inject lateinit var debugTree: Timber.DebugTree
    @Inject lateinit var appSettingsDataStore: DataStore<AppSettings>
    @Inject lateinit var authInteractor: AuthInteractor
    @Inject lateinit var crashlytics: FirebaseCrashlytics
    @Inject lateinit var analytics: FirebaseAnalytics
    @Inject lateinit var settingsInteractor: SettingsInteractor
    @Inject @CoroutineScopeIO lateinit var coroutineScopeIO: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        initAds()
        initLogging()
    }

    private fun initAds() {
        MobileAds.initialize(applicationContext)
    }

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(debugTree)
            Timber.d("Planting debugTree")
        }
        coroutineScopeIO.launch {
            settingsInteractor.userPreferences.collectLatest { preferences ->
                preferences?.let {
                    crashlytics.setUserId(it.userUUID ?: "")
                    analytics.setUserId(it.userUUID)
                    val collectingData = Timber.forest().contains(crashlyticsTree)
                    if (it.analyticsEnabled && !collectingData) {
                        Timber.d("Analytics On, Planting crashlyticsTree")
                        Timber.plant(crashlyticsTree)
                        crashlytics.setCrashlyticsCollectionEnabled(true)
                        analytics.setAnalyticsCollectionEnabled(true)
                    } else if (!it.analyticsEnabled && collectingData) {
                        Timber.uproot(crashlyticsTree)
                        crashlytics.setCrashlyticsCollectionEnabled(false)
                        analytics.setAnalyticsCollectionEnabled(false)
                        crashlytics.deleteUnsentReports()
                        Timber.d("Analytics Off, Uprooting crashlyticsTree")
                    }
                }
            }
        }
    }
}