plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ascendy.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ascendy.app"
        minSdk = 26
        targetSdk = 35
        val envCode = System.getenv("ASCENDY_VERSION_CODE")?.toIntOrNull()
        versionCode = envCode ?: 30
        versionName = "0.3.${versionCode}"
        buildConfigField("String", "RELEASE_REPO", "\"bandersong/ascendy\"")
    }

    // ───── Distribution flavors ─────
    // foss: keeps the in-app GitHub-Releases updater + REQUEST_INSTALL_PACKAGES.
    // play: stripped — no in-app installer; updates flow through Play.
    flavorDimensions += "distribution"
    productFlavors {
        create("foss") {
            dimension = "distribution"
            buildConfigField("Boolean", "HAS_INAPP_UPDATER", "true")
        }
        create("play") {
            dimension = "distribution"
            buildConfigField("Boolean", "HAS_INAPP_UPDATER", "false")
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        // Release config — only created when CI has provided the secrets. Locally, releases fall
        // back to debug signing so `assembleRelease` still produces a working APK for testing.
        val rsPwd = System.getenv("ASCENDY_RELEASE_STORE_PASSWORD")
        val rkPwd = System.getenv("ASCENDY_RELEASE_KEY_PASSWORD")
        val rkAlias = System.getenv("ASCENDY_RELEASE_KEY_ALIAS")
        val rksB64 = System.getenv("ASCENDY_RELEASE_KEYSTORE_BASE64")
        if (!rsPwd.isNullOrBlank() && !rkPwd.isNullOrBlank() && !rkAlias.isNullOrBlank() && !rksB64.isNullOrBlank()) {
            create("release") {
                val ksFile = layout.buildDirectory.file("generated-keystore/release.keystore").get().asFile
                ksFile.parentFile.mkdirs()
                ksFile.writeBytes(java.util.Base64.getDecoder().decode(rksB64))
                storeFile = ksFile
                storePassword = rsPwd
                keyAlias = rkAlias
                keyPassword = rkPwd
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-service:2.8.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // QR / barcode — scan + generate
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
