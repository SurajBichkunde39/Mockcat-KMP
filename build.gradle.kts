import org.gradle.api.file.FileTreeElement
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * Subprojects: apply ktlint in each module. Run: ./gradlew ktlintCheck detekt
 */
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.androidxRoom) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply true
}
allprojects {
    group = "com.mockcat"
    version = "0.1.0-SNAPSHOT"
}

// Root .ktlintignore is not always honored for generated MPP path sets; exclude all build/ output
subprojects {
    afterEvaluate {
        if (!plugins.hasPlugin("org.jlleitschuh.gradle.ktlint")) return@afterEvaluate
        extensions.configure<KtlintExtension>("ktlint") {
            filter {
                // Inline only (no script `private` helpers): required for configuration cache and closure capture
                exclude { e: FileTreeElement ->
                    val p = e.file.toString().replace('\\', '/')
                    p.contains("/build/") || p.endsWith("/build") || p.contains("/.gradle/") || p.endsWith("/.gradle")
                }
            }
        }
    }
}
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(file("$rootDir/config/detekt.yml"))
    baseline = file("$rootDir/config/detekt-baseline.xml")
}
