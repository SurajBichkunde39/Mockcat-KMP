import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}
kotlin {
    android {
        namespace = "com.mockcat.ui"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
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
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
