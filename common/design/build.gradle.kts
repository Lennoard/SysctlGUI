plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "${AppConfig.appId}.design"
    compileSdk = AppConfig.compileSdkVersion

    defaultConfig {
        minSdk = AppConfig.minSdkVersion
        targetSdk = AppConfig.targetSdkVersion

        testInstrumentationRunner = AppConfig.testInstrumentationRunner
        consumerProguardFiles(AppConfig.proguardConsumerRules)
    }

    buildTypes {
        release {
            isMinifyEnabled = !AppConfig.devCycle
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Compose.kotlinCompilerExtensionVersion
    }
}

dependencies {
    val composeBom = platform(Compose.BoM)
    api(composeBom)
    androidTestImplementation(composeBom)

    api(AndroidX.activity)
    api(AndroidX.appCompat)
    api(AndroidX.constraintLayout)
    api(AndroidX.core)
    api(AndroidX.swipeRefreshLayout)
    api(Compose.material3)
    api(Compose.material)
    api(Compose.activity)
    api(Compose.uiTooling)
    debugApi(Compose.uiTooling)
    implementation(AndroidX.splashScreen)
    implementation(Google.material)
}
