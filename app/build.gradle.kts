plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Firebase (Analytics / Crashlytics / Remote Config) chỉ kích hoạt khi đã đặt
// google-services.json (tải từ Firebase console) vào thư mục app/.
// Chưa có file -> vẫn build & chạy bình thường, các wrapper Firebase tự no-op.
val hasGoogleServices = file("google-services.json").exists()
if (hasGoogleServices) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
} else {
    logger.lifecycle("SnapEffect: app/google-services.json không tồn tại -> bỏ qua Firebase (Analytics/Crashlytics/Remote Config sẽ no-op).")
}

// ---------------------------------------------------------------------------
// Ad IDs theo build type. AdConfig.kt đọc các giá trị này qua BuildConfig.
//
// - Build DEBUG:   LUÔN dùng bộ ID TEST chính thức của Google (bên dưới).
//                  Không bao giờ đặt ID thật vào debug - tự click ad thật
//                  là invalid traffic, có thể bị khóa tài khoản AdMob.
// - Build RELEASE: dùng bộ ID THẬT. Thay toàn bộ "REPLACE_ME" bằng ID lấy từ
//                  AdMob console (mỗi placement 1 ad unit riêng). Khi còn
//                  REPLACE_ME, assembleRelease/bundleRelease sẽ FAIL có chủ đích
//                  (xem task check cuối file).
// ---------------------------------------------------------------------------

val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testAdUnitIds = mapOf(
    "AD_UNIT_NATIVE_SPLASH" to "ca-app-pub-3940256099942544/2247696110",
    "AD_UNIT_NATIVE_LANGUAGE" to "ca-app-pub-3940256099942544/2247696110",
    "AD_UNIT_NATIVE_ONBOARDING" to "ca-app-pub-3940256099942544/2247696110",
    "AD_UNIT_NATIVE_MEDIUM" to "ca-app-pub-3940256099942544/2247696110",
    "AD_UNIT_INTERSTITIAL_ONBOARDING" to "ca-app-pub-3940256099942544/1033173712",
    "AD_UNIT_INTERSTITIAL_SAVE" to "ca-app-pub-3940256099942544/1033173712",
    "AD_UNIT_BANNER" to "ca-app-pub-3940256099942544/6300978111",
    "AD_UNIT_APP_OPEN" to "ca-app-pub-3940256099942544/9257395921",
)

// ID THẬT cho release - điền dạng "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy".
// LƯU Ý: giữ đủ key giống testAdUnitIds (thiếu key nào BuildConfig release thiếu field đó).
val releaseAdmobAppId = "ca-app-pub-8164347892343683~6472650402" // App ID dạng "ca-app-pub-xxxxxxxxxxxxxxxx~zzzzzzzzzz"
val releaseAdUnitIds = mapOf(
    "AD_UNIT_NATIVE_SPLASH" to "ca-app-pub-8164347892343683/9545146660",
    "AD_UNIT_NATIVE_LANGUAGE" to "ca-app-pub-8164347892343683/3991924312",
    "AD_UNIT_NATIVE_ONBOARDING" to "ca-app-pub-8164347892343683/5414329964",
    "AD_UNIT_NATIVE_MEDIUM" to "ca-app-pub-8164347892343683/6995573166",
    "AD_UNIT_INTERSTITIAL_ONBOARDING" to "ca-app-pub-8164347892343683/6968451767",
    // Interstitial show sau khi Lưu ảnh - tạo unit mới trong AdMob console rồi điền vào đây
    "AD_UNIT_INTERSTITIAL_SAVE" to "ca-app-pub-8164347892343683/7046975836",
    "AD_UNIT_BANNER" to "ca-app-pub-8164347892343683/5352685986",
    "AD_UNIT_APP_OPEN" to "ca-app-pub-8164347892343683/9642716564",
)

android {
    namespace = "com.mpcorporation.snapeffect"
    compileSdk = 37

    packaging {
        jniLibs {
            excludes += listOf("**/libyuv-decoder.so")
        }
    }

    defaultConfig {
        applicationId = "com.mpcorporation.snapeffect"
        minSdk = 28
        targetSdk = 36
        versionCode = 17
        versionName = "3.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            // AdMob App ID thật (khớp meta-data trong AndroidManifest qua placeholder)
            manifestPlaceholders["admobAppId"] = releaseAdmobAppId
            releaseAdUnitIds.forEach { (name, id) ->
                buildConfigField("String", name, "\"$id\"")
            }

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
        debug {
            // AdMob App ID test
            manifestPlaceholders["admobAppId"] = testAdmobAppId
            testAdUnitIds.forEach { (name, id) ->
                buildConfigField("String", name, "\"$id\"")
            }

            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // Các ngôn ngữ chỉ dịch phần UI onboarding (splash/language/onboarding), phần còn
        // lại fallback về default values/.
        disable += "MissingTranslation"
    }
}

dependencies {
    // appcompat + material: chỉ còn phục vụ @style/Theme.SnapEffect và layout template native ad
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.exifinterface)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.process)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Compose - toàn bộ UI của app
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    // Google Fonts tải động (Plus Jakarta Sans + DM Mono) - có fallback font hệ thống
    implementation(libs.androidx.ui.text.google.fonts)

    // CameraX - camera chụp trong app
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ML Kit Face Detection - retouch nhắm khuôn mặt (on-device)
    implementation(libs.mlkit.face.detection)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Google Mobile Ads SDK
    implementation(libs.play.services.ads)
    // UMP - consent GDPR/EEA (bắt buộc cho traffic châu Âu)
    implementation(libs.user.messaging.platform)

    // Play In-App Review - đánh giá app ngay trong app (màn Cài đặt)
    implementation(libs.play.review)

    // Firebase - SDK luôn compile sẵn; chỉ hoạt động khi có google-services.json
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Chặn build release khi chưa điền ID thật - tránh ship nhầm placeholder (ad không load,
// doanh thu = 0 mà không có báo lỗi nào). Điền đủ ID ở đầu file này là build được.
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        val unfilled = buildList {
            if (releaseAdmobAppId.contains("REPLACE_ME")) add("admobAppId")
            releaseAdUnitIds.forEach { (name, id) -> if (id.contains("REPLACE_ME")) add(name) }
        }
        if (unfilled.isNotEmpty()) {
            throw GradleException(
                "Build RELEASE đang thiếu Ad ID thật (còn REPLACE_ME): ${unfilled.joinToString()}. " +
                    "Điền ID từ AdMob console vào releaseAdmobAppId / releaseAdUnitIds trong app/build.gradle.kts."
            )
        }
    }
}
