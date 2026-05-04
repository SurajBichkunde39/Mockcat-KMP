plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}
kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":mockcat-api"))
                implementation(project(":mockcat-persistence"))
                implementation(project(":mockcat-ui"))
                implementation(libs.androidxActivityCompose)
                implementation(libs.composeMaterial3)
                implementation(libs.composeUi)
            }
        }
    }
}
android {
    namespace = "com.mockcat.sample"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        applicationId = "com.mockcat.sample"
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true }
    packaging { resources { excludes += setOf("META-INF/**") } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes { getByName("debug") { isDefault = true } }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
