plugins {
    id("com.android.application")
}

// CI passes the version (-PversionName=1.2.0 -PversionCode=42); a local build is 0.0.0-dev, code 1.
val appVersionName = providers.gradleProperty("versionName").getOrElse("0.0.0-dev")
val appVersionCode = providers.gradleProperty("versionCode").map(String::toInt).getOrElse(1)

// A release keystore, if the owner made one: its path and passwords in the environment (the
// workflow's secrets, README "Signing"). Without one the release build is signed with the debug key.
val keystore = providers.environmentVariable("HALLWAY_KEYSTORE").orNull

android {
    namespace = "io.github.ariamokr.hallway"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.github.ariamokr.hallway"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = providers.environmentVariable("HALLWAY_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("HALLWAY_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("HALLWAY_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
