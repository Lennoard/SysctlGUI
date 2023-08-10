import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    namespace = AppConfig.appId
    compileSdk = AppConfig.compileSdkVersion

    defaultConfig {
        applicationId = AppConfig.appId
        minSdk = AppConfig.minSdkVersion
        targetSdk = AppConfig.targetSdlVersion
        versionCode = 11
        versionName = "2.0.0"
        vectorDrawables.useSupportLibrary = true
        resourceConfigurations.addAll(listOf("en", "de", "pt-rBR"))
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }
    }

    signingConfigs {
        create("release") {
            val localPropFile = rootProject.file("local.properties")
            val keystoreProps = Properties().apply {
                load(localPropFile.inputStream())
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
                "proguard-rules.pro",
                "proguard-kt.pro"
            )
        }
    }

    buildFeatures {
        android.buildFeatures.viewBinding = true
        android.buildFeatures.dataBinding = true
        compose = true
    }

    sourceSets {
        maybeCreate("main").java.srcDir("src/main/kotlin")
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    packagingOptions {
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Compose.kotlinCompilerExtensionVersion
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))

    implementation(project(Modules.domain))
    implementation(project(Modules.data))
    implementation(project(Modules.utils))
    implementation(project(Modules.design))

    implementation(AndroidX.splashScreen)
    implementation(AndroidX.lifecycleLiveData)
    implementation(AndroidX.lifecycleRuntimeCompose)
    implementation(AndroidX.navigationFragment)
    implementation(AndroidX.navigationUi)
    implementation(AndroidX.preference)
    implementation(AndroidX.room)
    implementation(AndroidX.roomRuntime)
    kapt(AndroidX.roomCompiler)

    implementation(Google.gson)

    implementation(Dependencies.koinAndroid)
    implementation(Dependencies.libSuCore)
    implementation(Dependencies.liveEvent)
    implementation(Dependencies.tapTargetView)
}
