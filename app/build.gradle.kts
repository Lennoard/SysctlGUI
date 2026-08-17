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
        versionCode = 24
        versionName = "3.2.0"
        vectorDrawables.useSupportLibrary = true
        androidResources {
            localeFilters += listOf("en", "de", "pt-rBR", "tr", "zh-rCN")
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
            val envStorePath = System.getenv("KEYSTORE_PATH")
            val envStorePassword = System.getenv("KEYSTORE_PASSWORD")
            val envKeyAlias = System.getenv("KEY_ALIAS")
            val envKeyPassword = System.getenv("KEY_PASSWORD")

            if (!envStorePath.isNullOrBlank()) {
                storeFile = file(envStorePath)
                storePassword = envStorePassword ?: ""
                keyAlias = envKeyAlias ?: ""
                keyPassword = envKeyPassword ?: ""
                return@create
            }

            val propFile = rootProject.file("keystore.properties")
            if (!propFile.exists()) {
                propFile.createNewFile()
            }
            val keystoreProps = Properties().apply {
                load(propFile.inputStream())
            }

            val storeFilePath = keystoreProps["storeFile"] as? String
            val keyAliasValue = keystoreProps["keyAlias"] as? String
            val keyPasswordValue = keystoreProps["keyPassword"] as? String
            val storePasswordValue = keystoreProps["storePassword"] as? String

            keyAlias = keyAliasValue ?: ""
            keyPassword = keyPasswordValue ?: ""
            storeFile = if (!storeFilePath.isNullOrBlank()) file(storeFilePath) else file("/dev/null")
            storePassword = storePasswordValue ?: ""
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            val envEnabled = !System.getenv("KEYSTORE_PATH").isNullOrBlank() &&
                    !System.getenv("KEYSTORE_PASSWORD").isNullOrBlank() &&
                    !System.getenv("KEY_ALIAS").isNullOrBlank() &&
                    !System.getenv("KEY_PASSWORD").isNullOrBlank()
            val propFile = rootProject.file("keystore.properties")
            val keyStoreExists = propFile.exists() && propFile.readText().contains("storeFile")
            val hasSigningValues = keyStoreExists && propFile.readText().contains("keyAlias") &&
                    propFile.readText().contains("storePassword")
            signingConfig = if (envEnabled || hasSigningValues) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
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
