import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

group = "com.mockcat"
version = rootProject.version

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

application {
    mainClass.set("com.mockcat.sample.server.ServerKt")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktorServerCore)
    implementation(libs.ktorServerNetty)
    implementation(libs.ktorServerCors)
    implementation(libs.ktorServerContentNegotiation)
    implementation(libs.ktorSerializationKotlinxJson)
}

ktlint { filter { exclude { it.file.path.contains("/build/") } } }
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }
