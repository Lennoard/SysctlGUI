plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "${AppConfig.appId}.data"
    compileSdk = AppConfig.compileSdkVersion

    defaultConfig {
        minSdk = AppConfig.minSdkVersion
        targetSdk = AppConfig.targetSdkVersion
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += mapOf("clearPackageData" to "true")

        testOptions {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }



    sourceSets {
        maybeCreate("main").java.srcDir("src/main/kotlin")
    }
}

dependencies {
    implementation(project(Modules.domain))
    implementation(project(Modules.utils))

    implementation(AndroidX.preference)
    implementation(AndroidX.room)
    implementation(AndroidX.roomRuntime)
    kapt(AndroidX.roomCompiler)

    implementation(Dependencies.libSuCore)
    implementation(Google.gson)

    implementation(Dependencies.koinAndroid)

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
