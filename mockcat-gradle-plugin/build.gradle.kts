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
// mockcat-api is compiled to class file version 65 (JDK 21); the test JVM must be able to load it.
tasks.named<Test>("test") {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(24))
        },
    )
}
dependencies {
    implementation(gradleApi())
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":mockcat-api"))
    // Read applicationId from the Android extension at configuration time; AGP is always on the
    // classpath when the plugin is applied to an Android project.
    compileOnly("com.android.tools.build:gradle:${libs.versions.agp.get()}")

    testImplementation(gradleTestKit())
    testImplementation(libs.junit)
}
gradlePlugin {
    plugins {
        create("mockcat") {
            id = "com.mockcat.mockcat-gradle"
            implementationClass = "com.mockcat.gradle.MockcatPlugin"
        }
    }
}
