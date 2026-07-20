import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val signingStoreFile = providers.gradleProperty("signingStoreFile")
val signingStorePassword = providers.gradleProperty("signingStorePassword")
val signingKeyAlias = providers.gradleProperty("signingKeyAlias")
val signingKeyPassword = providers.gradleProperty("signingKeyPassword")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("io.github.xpler2.compiler")
}

android {
    namespace = "io.github.xpler.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.xpler.example"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseSigningConfig = if (
        signingStoreFile.isPresent &&
        signingStorePassword.isPresent &&
        signingKeyAlias.isPresent &&
        signingKeyPassword.isPresent
    ) {
        signingConfigs.create("release") {
            storeFile = file(signingStoreFile.get())
            storePassword = signingStorePassword.get()
            keyAlias = signingKeyAlias.get()
            keyPassword = signingKeyPassword.get()
        }
    } else {
        null
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = releaseSigningConfig
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":xpler2-api"))
    implementation(project(":xpler2-xposed"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
}
