// Top-level
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}