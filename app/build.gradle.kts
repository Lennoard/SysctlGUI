import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android-extensions")
}

android {
    buildToolsVersion("29.0.3")

    compileSdkVersion(29)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    defaultConfig {
        applicationId = "com.androidvip.sysctlgui"
        minSdkVersion(19)
        targetSdkVersion(29)
        versionCode = 9
        versionName = "1.0.8"
        resConfigs("en", "de", "pt-rBR")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-kt.pro"
            )
        }
    }

    /*buildFeatures {
        viewBinding = true
        dataBinding = true
    }*/

    sourceSets {
        maybeCreate("main").java.srcDir("src/main/kotlin")
    }

    packagingOptions {
        exclude("/META-INF/**")
        exclude("/androidsupportmultidexversion.txt")
        exclude("/kotlin/**")
        exclude("/kotlinx/**")
        exclude("/okhttp3/**")
        exclude("/*.txt")
        exclude("/*.bin")
    }
}

android.applicationVariants.forEach { variant ->
    val defaultConfig = android.defaultConfig
    variant.outputs.all {
        var fileName = "sysctlgui"
        fileName += "-v${defaultConfig.versionName}(${defaultConfig.versionCode})"
        fileName += if (variant.buildType.name == "release") ".apk" else "-SNAPSHOT.apk"

        outputFile.renameTo(File(outputFile.path, fileName))
    }
}

kapt {
    //generateStubs = true
    correctErrorTypes = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7")

    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.google.android.material:material:1.3.0")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation("com.daimajia.androidanimations:library:2.3@aar")
    implementation("com.daimajia.easing:library:2.1@aar")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.11.0")
    implementation("com.github.topjohnwu.libsu:core:2.5.1")
}
