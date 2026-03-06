import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.stability.analyzer)
}

android {
    namespace = AppConfig.appId
    compileSdk = AppConfig.compileSdkVersion

    defaultConfig {
        applicationId = AppConfig.appId
        minSdk = AppConfig.minSdkVersion
        targetSdk = AppConfig.targetSdkVersion
        versionCode = 23
        versionName = "3.1.0"
        vectorDrawables.useSupportLibrary = true
        androidResources {
            localeFilters += listOf("en", "de", "pt-rBR", "tr")
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }
        multiDexEnabled = true
    }

    signingConfigs {
        create("release") {
            val propFile = rootProject.file("keystore.properties")
            if (!propFile.exists()) {
                propFile.createNewFile()
            }
            val keystoreProps = Properties().apply {
                load(propFile.inputStream())
            }

            keyAlias = keystoreProps["keyAlias"] as? String ?: ""
            keyPassword = keystoreProps["keyPassword"] as? String ?: ""
            storeFile = file(keystoreProps["storeFile"] as? String ?: "/")
            storePassword = keystoreProps["storePassword"] as? String ?: ""
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = !AppConfig.devCycle
            isShrinkResources = !AppConfig.devCycle
            isDebuggable = AppConfig.devCycle
            signingConfig = signingConfigs.getByName("release")
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

    packaging {
        resources.excludes.addAll(
            arrayOf(
                "/META-INF/**",
                "/androidsupportmultidexversion.txt",
                "/kotlin/**",
                "/kotlinx/**",
                "/okhttp3/**",
                "/*.txt",
                "/*.bin"
            )
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(project(":common:design"))
    implementation(project(":common:utils"))
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.window)
    implementation(libs.androidx.work.runtime.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    //ksp(libs.androidx.lifecycle.compiler)

    implementation(libs.koin)
    implementation(libs.koin.compose)
    implementation(libs.bundles.libsu)
}
