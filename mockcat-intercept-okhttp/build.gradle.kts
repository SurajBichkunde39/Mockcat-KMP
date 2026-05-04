plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}
kotlin {
    androidTarget { publishAllLibraryVariants() }
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":mockcat-api"))
                implementation(libs.okhttp)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
android {
    namespace = "com.mockcat.intercept.okhttp"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
