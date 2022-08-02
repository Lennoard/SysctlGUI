import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = 32
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf("clearPackageData" to "true")
        minSdk = 21
        targetSdk = 32
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        testOptions {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }
    }

    sourceSets {
        maybeCreate("main").java.srcDir("src/main/kotlin")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":common:utils"))

    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

    implementation("androidx.appcompat:appcompat:1.6.0-alpha05")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.room:room-ktx:2.4.0")
    implementation("androidx.room:room-runtime:2.4.0")

    implementation("com.github.topjohnwu.libsu:core:2.5.1")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation("io.insert-koin:koin-android:3.1.3")

    kapt("androidx.room:room-compiler:2.4.0")

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
