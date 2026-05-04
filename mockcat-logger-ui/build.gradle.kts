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
    applyDefaultHierarchyTemplate()
    android {
        namespace = "com.mockcat.logger.ui"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "MockcatLoggerUI"
            isStatic = true
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":mockcat-logger"))
                implementation(project(":mockcat-api"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.composeRuntime)
                implementation(libs.composeFoundation)
                implementation(libs.composeUi)
                implementation(libs.composeMaterial3)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidxActivityCompose)
                implementation(libs.androidx.lifecycle.runtime.compose)
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(libs.composeUi)
                implementation(libs.composeRuntime)
                implementation(libs.composeMaterial3)
                implementation(project(":mockcat-logger-persistence"))
            }
        }
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
publishing { publications { } }
