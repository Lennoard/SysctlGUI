import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    namespace = AppConfig.appId
    compileSdk = AppConfig.compileSdkVersion

    defaultConfig {
        applicationId = AppConfig.appId
        minSdk = AppConfig.minSdkVersion
        targetSdk = AppConfig.targetSdkVersion
        versionCode = 16
        versionName = "2.2.2"
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
                "proguard-rules.pro",
                "proguard-kt.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

    sourceSets {
        maybeCreate("main").java.srcDir("src/main/kotlin")
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Compose.kotlinCompilerExtensionVersion
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))

    implementation(project(Modules.domain))
    implementation(project(Modules.data))
    implementation(project(Modules.utils))
    implementation(project(Modules.design))

    implementation(AndroidX.activity)
    implementation(AndroidX.splashScreen)
    implementation(AndroidX.lifecycleLiveData)
    implementation(AndroidX.lifecycleRuntimeCompose)
    implementation(AndroidX.navigationFragment)
    implementation(AndroidX.navigationUi)
    implementation(AndroidX.preference)
    implementation(AndroidX.room)
    implementation(AndroidX.roomRuntime)
    implementation(AndroidX.workManager)
    ksp(AndroidX.roomCompiler)

    implementation(Google.gson)

    implementation(Dependencies.koinAndroid)
    implementation(Dependencies.libSuCore)
    implementation(Dependencies.libSuIo)
    implementation(Dependencies.liveEvent)
    implementation(Dependencies.tapTargetView)
}
