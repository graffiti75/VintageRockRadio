// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
}

// It's good practice to define versions in libs.versions.toml,
// so this file can remain relatively clean.
// Ensure libs.versions.toml is present, usually in root or ./gradle/
// For this exercise, I'll create it in the root, as done previously.

Unit // Required for a build.gradle.kts file to be valid if it doesn't have other statements that return Unit.
