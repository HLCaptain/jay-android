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

buildscript {
    repositories {
//        google()
//        mavenCentral()
    }
}

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.google.gms.services) apply false
    alias(libs.plugins.google.secrets) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.junit5) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.ksp) apply false
}

sonarqube {
    properties {
        property("sonar.projectKey", "HLCaptain_jay-android")
        property("sonar.organization", "hlcaptain")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
