import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.devtools.ksp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

// Version Management - Pattern: x.y where y goes from 0-9, then x increments
val versionPropsFile = file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        load(FileInputStream(versionPropsFile))
    } else {
        this["VERSION_CODE"] = "1"
        this["VERSION_MAJOR"] = "0"
        this["VERSION_MINOR"] = "5"
    }
}

var vCode = versionProps["VERSION_CODE"].toString().toInt()
var vMajor = versionProps["VERSION_MAJOR"].toString().toInt()
var vMinor = versionProps["VERSION_MINOR"].toString().toInt()

// Auto-increment on release builds (works with Android Studio Generate Signed APK/Bundle)
val isReleaseBuild = gradle.startParameter.taskNames.any {
    it.contains("Release", ignoreCase = true) &&
            (it.contains("assemble", ignoreCase = true) || it.contains("bundle", ignoreCase = true))
}

if (isReleaseBuild) {
    vCode += 1
    if (vMinor >= 9) {
        vMajor += 1
        vMinor = 0
    } else {
        vMinor += 1
    }
    versionProps["VERSION_CODE"] = vCode.toString()
    versionProps["VERSION_MAJOR"] = vMajor.toString()
    versionProps["VERSION_MINOR"] = vMinor.toString()
    versionProps.store(FileOutputStream(versionPropsFile), "Version properties - Pattern: x.y (y: 0-9)")
    println("Version bumped to: $vMajor.$vMinor (code: $vCode)")
}

val vName = "$vMajor.$vMinor"

android {
    namespace = "dev.sebastianrn.portfolioapp"
    compileSdk = 36 // Targeting Android 14/15 (Safe stable version)

    defaultConfig {
        applicationId = "dev.sebastianrn.portfolioapp"
        minSdk = 26
        targetSdk = 36
        versionCode = vCode
        versionName = vName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val apiKey = localProperties.getProperty("GOLD_API_KEY") ?: ""
        buildConfigField("String", "GOLD_API_KEY", "\"$apiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/INDEX.LIST"
        }
    }
}

dependencies {

    // Core Android & Kotlin
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose BOM (Bill of Materials) - Manages versions automatically
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.google.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)

    // Room Database (Kotlin + KSP)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Navigation (Compose)
    implementation(libs.androidx.navigation.compose)

    // Vico Charts (Modern Graphing Library)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)

    // Networking (Retrofit + Gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Unit Testing (Local)
    testImplementation(libs.junit)
    testImplementation(libs.mockk) // For mocking classes
    testImplementation(libs.kotlinx.coroutines.test) // For testing Coroutines
    testImplementation(libs.turbine) // For testing Flows
    testImplementation(libs.androidx.core.testing)

    // Instrumented Testing (Device/Emulator)
    androidTestImplementation(libs.androidx.room.testing) // Specific Room tester

    implementation(libs.jsoup)

    // WorkManager for scheduled backups
    implementation(libs.work.runtime.ktx)
}

// Version task - prints current version
tasks.register("printVersion") {
    group = "versioning"
    description = "Print current version information"
    doLast {
        println("Current version: $vMajor.$vMinor (code: $vCode)")
    }
}