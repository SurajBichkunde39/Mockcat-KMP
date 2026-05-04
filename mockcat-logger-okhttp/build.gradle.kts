import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}
kotlin {
    android {
        namespace = "com.mockcat.logger.okhttp"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    sourceSets {
        val androidMain by getting {
            dependencies {
                api(project(":mockcat-logger-core"))
                implementation(project(":mockcat-api"))
                implementation(libs.okhttp)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
publishing { publications { } }
