plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "vn.navitaximap"
    compileSdk = 35

    defaultConfig {
        applicationId = "vn.navitaximap"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-dev"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // OpenGL ES variant is the conservative choice for Android 13+ phones.
    implementation("org.maplibre.gl:android-sdk-opengl:13.5.4")
    testImplementation("junit:junit:4.13.2")
}
