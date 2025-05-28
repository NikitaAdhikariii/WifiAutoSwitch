plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.wifiautoswitch" // ✅ REQUIRED
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wifiautoswitch"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}