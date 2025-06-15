
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mpcorporation.snapeffect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mpcorporation.snapeffect"
        minSdk = 28
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {

    implementation("com.github.yalantis:ucrop:2.2.10")
    implementation("com.github.yalantis:ucrop:2.2.9-native")

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation (libs.gpuimage.v210)

    implementation(libs.glide)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}