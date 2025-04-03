// Top-level
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler)
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}