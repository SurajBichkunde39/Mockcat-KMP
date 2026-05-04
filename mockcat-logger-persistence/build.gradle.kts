/**
 * HTTP traffic log storage (separate DB from [mockcat-intercept-persistence] mock rules). Same Room+KMP
 * pattern as mock rules; see AGENT.md.
 */
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}
kotlin {
    androidTarget { publishAllLibraryVariants() }
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework { baseName = "MockcatLoggerPersistence" }
    }
    sourceSets {
        all { languageSettings { optIn("kotlin.RequiresOptIn") } }
        val commonMain by getting {
            dependencies {
                implementation(project(":mockcat-api"))
                implementation(project(":mockcat-logger"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.roomRuntime)
                implementation(libs.sqliteBundled)
            }
        }
        val commonTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
    }
}
room {
    schemaDirectory("$projectDir/schemas")
}
dependencies {
    add("kspAndroid", libs.roomCompiler)
    add("kspIosArm64", libs.roomCompiler)
    add("kspIosSimulatorArm64", libs.roomCompiler)
}
android {
    namespace = "com.mockcat.logger.persistence"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.androidMinSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
