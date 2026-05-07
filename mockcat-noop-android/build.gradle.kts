import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}

android {
    namespace = "com.mockcat.noop"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.androidMinSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing { singleVariant("release") }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    // Lightweight interface-only modules — no Room, no Compose, no persistence.
    api(project(":mockcat-api"))
    api(project(":mockcat-logger-core"))
    // OkHttp: needed for Interceptor type in no-op interceptor classes.
    implementation(libs.okhttp)
    // Ktor client: needed for HttpClient + plugin types in no-op Ktor adapters.
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientOkhttp)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.mockcat"
            artifactId = "mockcat-noop-android"
            afterEvaluate { from(components["release"]) }
        }
    }
}

detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
