plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}
kotlin {
    androidTarget { publishAllLibraryVariants() }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mockcat-api"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.composeRuntime)
                implementation(libs.composeFoundation)
                implementation(libs.composeUi)
                implementation(libs.composeUiToolingPreview)
                implementation(libs.composeMaterial3)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":mockcat-persistence"))
                implementation(libs.androidxActivityCompose)
            }
        }
    }
}
android {
    namespace = "com.mockcat.ui"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
