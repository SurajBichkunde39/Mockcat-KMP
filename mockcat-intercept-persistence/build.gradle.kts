import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Room (KMP) store for mock rules. KSP + Room for common `expect/actual` use the Room plugin +
 * `kspAndroid` / `kspIos*`; see AGENT.md.
 */
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}
kotlin {
    android {
        namespace = "com.mockcat.persistence"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework { baseName = "MockcatPersistence" }
    }
    sourceSets {
        all { languageSettings { optIn("kotlin.RequiresOptIn") } }
        val commonMain by getting {
            dependencies {
                implementation(project(":mockcat-api"))
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
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
