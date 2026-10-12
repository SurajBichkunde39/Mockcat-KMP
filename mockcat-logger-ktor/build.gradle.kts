import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
}
kotlin {
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    android {
        namespace = "com.mockcat.logger.ktor"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":mockcat-logger-core"))
                implementation(project(":mockcat-api"))
                implementation(libs.ktorClientCore)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":mockcat-logger-persistence"))
            }
        }
    }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
publishing { publications { } }
// Match the Kotlin jvmTarget so Java tasks don't follow the host JDK (e.g. 21).
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}
