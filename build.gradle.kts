// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Existing plugins block from previous attempt was:
// plugins {
//    alias(libs.plugins.androidApplication) apply false
//    alias(libs.plugins.kotlinAndroid) apply false
// }
// However, the libs alias won't be available until libs.versions.toml is sourced by settings.gradle.
// A common practice for root build.gradle.kts is to define plugin versions directly if not using a plugins block that itself depends on libs.
// Or, ensure settings.gradle.kts makes libs available.
// For now, let's keep it simple and assume the plugin versions are managed via settings/libs.versions.toml

plugins {
    id("com.android.application") version "8.2.0" apply false // Match version in libs.versions.toml
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false // Match version in libs.versions.toml
}

Unit
