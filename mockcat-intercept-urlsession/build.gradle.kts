plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}
kotlin {
    applyDefaultHierarchyTemplate()
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework { baseName = "MockcatInterceptUrlsession" }
    }
    sourceSets {
        val iosMain by getting {
            dependencies {
                implementation(project(":mockcat-api"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
