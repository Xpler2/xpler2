import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    id("com.vanniktech.maven.publish")
    id("signing")
}

android {
    namespace = "io.github.xpler2"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

dependencies {
    compileOnly(libs.xposed.api)
    compileOnly(libs.libxposed.api)
    compileOnly(libs.libxposed.service)
    compileOnly(project(":xpler2-compat"))
    implementation(libs.androidx.core.ktx)
}

mavenPublishing {
    coordinates("io.github.xpler2", "xpler2", "${project.properties["libVersion"]}")

    pom {
        name.set("xpler2-core")
        description.set("Xpler2 is a library for Xposed")
        url.set("https://github.com/Xpler2/xpler2")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                name.set("Gang")
                url.set("https://github.com/Xpler2/xpler2")
            }
        }
        scm {
            url.set("https://github.com/Xpler2/xpler2")
            connection.set("scm:git:git://github.com/Xpler2/xpler2.git")
            developerConnection.set("scm:git:ssh://git@github.com/Xpler2/xpler2.git")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}
