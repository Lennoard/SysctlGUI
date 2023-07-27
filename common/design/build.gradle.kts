plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "${AppConfig.appId}.design"
    compileSdk = AppConfig.compileSdkVersion

    defaultConfig {
        minSdk = AppConfig.minSdkVersion
        targetSdk = AppConfig.targetSdlVersion

        testInstrumentationRunner = AppConfig.testInstrumentationRunner
        consumerProguardFiles(AppConfig.proguardConsumerRules)
    }

    buildTypes {
        release {
            isMinifyEnabled = !AppConfig.devCycle
            isShrinkResources = !AppConfig.devCycle
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(AndroidX.appCompat)
    api(AndroidX.constraintLayout)
    api(AndroidX.core)
    api(AndroidX.swipeRefreshLayout)
    implementation(AndroidX.splashScreen)
    implementation(Google.material)
}
