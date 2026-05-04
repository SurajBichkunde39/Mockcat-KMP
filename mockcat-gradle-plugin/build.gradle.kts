import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}
repositories {
    mavenCentral()
    google()
}
group = "com.mockcat"
version = "0.1.0-SNAPSHOT"
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}
kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
dependencies {
    implementation(gradleApi())
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":mockcat-api"))
}
gradlePlugin {
    plugins {
        create("mockcat") {
            id = "com.mockcat.mockcat-gradle"
            implementationClass = "com.mockcat.gradle.MockcatPlugin"
        }
    }
}
