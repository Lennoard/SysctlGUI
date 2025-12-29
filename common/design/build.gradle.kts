plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "${AppConfig.appId}.design"
    compileSdk = AppConfig.compileSdkVersion

    defaultConfig {
        minSdk = AppConfig.minSdkVersion

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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.animation.graphics)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material)
    api(libs.androidx.compose.material3)
    api(libs.androidx.window)

    api(libs.material)

    androidTestApi(platform(libs.androidx.compose.bom))

    if (AppConfig.devCycle) {
        api(libs.androidx.compose.ui.tooling)
        api(libs.androidx.ui.test.manifest)
    } else {
        debugApi(libs.androidx.compose.ui.tooling)
        debugApi(libs.androidx.ui.test.manifest)
    }
}
