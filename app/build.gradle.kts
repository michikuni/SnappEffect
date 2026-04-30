
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mpcorporation.snapeffect"
    compileSdk = 36
    packaging {
        jniLibs {
            excludes += listOf("**/libyuv-decoder.so")
        }
    }
    defaultConfig {
        applicationId = "com.mpcorporation.snapeffect"
        minSdk = 28
        targetSdk = 35
        versionCode = 15
        versionName = "2.0.0"
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
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.exifinterface)
    implementation(libs.glide)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}