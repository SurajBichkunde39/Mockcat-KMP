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
                implementation(project(":mockcat-intercept-okhttp"))
                implementation(libs.ktorClientCore)
                implementation(libs.ktorClientOkhttp)
            }
        }
    }
}
android {
    namespace = "com.mockcat.intercept.ktor"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.androidMinSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
