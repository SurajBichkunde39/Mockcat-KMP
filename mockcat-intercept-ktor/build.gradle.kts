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
        namespace = "com.mockcat.intercept.ktor"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":mockcat-api"))
                implementation(project(":mockcat-intercept-okhttp"))
                implementation(libs.ktorClientCore)
                implementation(libs.ktorClientOkhttp)
            }
        }
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
