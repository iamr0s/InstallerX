@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.agp.lib)
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 19
        targetSdk = 33
    }

    buildFeatures {
        buildConfig = false
    }
}

dependencies {
}