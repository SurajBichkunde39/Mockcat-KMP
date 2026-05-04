rootProject.name = "mockcat"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":mockcat-api",
    ":mockcat-intercept-ui",
    ":mockcat-intercept-persistence",
    ":mockcat-logger-core",
    ":mockcat-logger-persistence",
    ":mockcat-logger-okhttp",
    ":mockcat-logger-ktor",
    ":mockcat-logger-urlsession",
    ":mockcat-okhttp-android",
    ":mockcat-logger-ui",
    ":mockcat-intercept-okhttp",
    ":mockcat-intercept-ktor",
    ":mockcat-intercept-urlsession",
    ":mockcat-integration-chucker",
    ":mockcat-gradle-plugin",
    ":sample-compose",
    ":sample-server",
)
