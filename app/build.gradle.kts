

plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.tapsss"
    compileSdk = 35

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a") // Adjust as needed
            isUniversalApk = true // This is key for your universal APK
        }
    }


    defaultConfig {
        applicationId = "com.tapsss"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.core.ktx)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)
    implementation(libs.okhttp)
    implementation(libs.viewpager2)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
//    implementation(libs.ffmpeg.kit.full.v44lts)
    implementation(libs.osmdroid.android)
    implementation(libs.osmdroid.wms)

    implementation(libs.ffmpeg.kit.video)
}