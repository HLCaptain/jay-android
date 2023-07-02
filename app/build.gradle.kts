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
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.gms.services)
    alias(libs.plugins.google.secrets)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.hilt)
    alias(libs.plugins.junit5)
    alias(libs.plugins.ksp)
}

android {
    compileSdk = 34

    namespace = "illyan.jay"

    defaultConfig {
        applicationId = "illyan.jay"
        minSdk = 21
        targetSdk = 34
        versionCode = 15
        versionName = "0.3.4-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        val mapboxAccessToken = properties["MAPBOX_ACCESS_TOKEN"].toString()
        val mapboxDownloadsToken = properties["MAPBOX_DOWNLOADS_TOKEN"].toString()
        val mapboxSdkRegistryToken = properties["SDK_REGISTRY_TOKEN"].toString()
        getByName("debug") {
            isDebuggable = true
            buildConfigField("String", "MapboxAccessToken", "\"$mapboxAccessToken\"")
            buildConfigField("String", "MapboxDownloadsToken", "\"$mapboxDownloadsToken\"")
            buildConfigField("String", "MapboxSdkRegistryToken", "\"$mapboxSdkRegistryToken\"")
        }
        getByName("release") {
            initWith(getByName("debug"))
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.orNull
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        animationsDisabled = true
    }

    lint {
        // If set to true (default), stops the build if errors are found.
        // This is needed because CICD pipeline will flop without it.
        abortOnError = false
    }
}

dependencies {
    // Core
    implementation(libs.jetbrains.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.google.material)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))

    // Compose
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Baseline Profiles and benchmarking
    implementation(libs.androidx.profileinstaller)

    // Scrollbar
    implementation(libs.hlcaptain.compose.scrollbar)

    // TODO: implement some way to use bio auth
    // Biometric Auth
    //implementation "androidx.biometric:biometric:1.2.0-alpha05"

    // Material design icons
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Item Swipe
    implementation(libs.saket.swipe)

    // Mapbox
    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.search)
    implementation(libs.mapbox.navigation)

    // Accompanist
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.placeholder.material)

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Timber
    implementation(libs.timber)

    // Navigation
    implementation(libs.compose.destinations.animations.core)
    ksp(libs.compose.destinations.ksp)

    // Coil
    implementation(libs.coil)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Serialization
    implementation(libs.androidx.datastore)
    implementation(libs.jetbrains.kotlinx.collections.immutable)
    implementation(libs.jetbrains.kotlinx.serialization.json)
    implementation(libs.jetbrains.kotlinx.serialization.protobuf)

    // Compression
    implementation(libs.zstd.jni) { artifact { type = "aar" } }
    testImplementation(libs.zstd.jni)

    // Coroutine
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.jetbrains.kotlinx.coroutines.android)
    implementation(libs.jetbrains.kotlinx.coroutines.test)

    // Google services
    implementation(libs.google.gms.play.services.location)
    implementation(libs.google.gms.play.services.auth)
    implementation(libs.google.gms.play.services.ads)
    implementation(libs.google.maps.utils)
    implementation(libs.google.maps.utils.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.perf.ktx)

    // JUnit5 testing tools
    // (Required) Writing and executing Unit Tests on the JUnit Platform
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    // (Optional) If you need "Parameterized Tests"
    testImplementation(libs.junit.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)

    // MockK test tool
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)

    // Other testing tools
    androidTestImplementation(libs.androidx.test.junit)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

kapt {
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = true
}