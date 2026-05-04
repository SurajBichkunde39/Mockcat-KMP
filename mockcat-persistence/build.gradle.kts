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
    iosArm64()
    iosSimulatorArm64()
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
        val androidUnitTest by getting
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
    namespace = "com.mockcat.persistence"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
