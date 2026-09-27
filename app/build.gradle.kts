plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.dfmtoy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dfmtoy"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }

    // Modules registered here become this app's Dynamic Feature Modules.
    dynamicFeatures += setOf(":countrycodekr", ":countrycodejp")

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // Library that provides SplitInstallManager (installedModules, on-demand install)
    implementation(libs.play.feature.delivery)
    implementation(libs.play.feature.delivery.ktx)
}
