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
                // [OkHttpClientFactory] / [MockcatLogging] — registers [HttpLogReader] for [MockcatLoggerUi] log activity.
                implementation(project(":mockcat-okhttp-android"))
                implementation(project(":mockcat-logger-ktor"))
                implementation(project(":mockcat-logger-ui"))
                implementation(libs.androidx.navigation3.runtime)
                implementation(libs.androidx.navigation3.ui)
                implementation(libs.androidx.lifecycle.viewmodel.navigation3)
                implementation(libs.androidxActivityCompose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.viewmodel.ktx)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.composeMaterial3)
                implementation(libs.composeFoundation)
                implementation(libs.composeUi)
                implementation(libs.okhttp)
                implementation(libs.ktorClientContentNegotiation)
                implementation(libs.ktorClientCore)
                implementation(libs.ktorClientOkhttp)
                implementation(libs.ktorSerializationKotlinxJson)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.chucker.library)
            }
        }
    }
}
android {
    namespace = "com.mockcat.sample"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
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
