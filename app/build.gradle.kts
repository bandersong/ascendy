import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("io.github.takahirom.roborazzi")
}

// Release signing secrets are injected by CI via env vars. Compute their presence once: it decides
// whether release builds get the real signing config, and the guard at the bottom of this file fails
// any release packaging that lacks them — so we never silently fall back to the public debug key.
val releaseStorePwd: String? = System.getenv("ASCENDY_RELEASE_STORE_PASSWORD")
val releaseKeyPwd: String? = System.getenv("ASCENDY_RELEASE_KEY_PASSWORD")
val releaseKeyAlias: String? = System.getenv("ASCENDY_RELEASE_KEY_ALIAS")
val releaseKeystoreB64: String? = System.getenv("ASCENDY_RELEASE_KEYSTORE_BASE64")
val hasReleaseSigning: Boolean = !releaseStorePwd.isNullOrBlank() && !releaseKeyPwd.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() && !releaseKeystoreB64.isNullOrBlank()

android {
    namespace = "com.ascendy.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ascendy.app"
        minSdk = 26
        targetSdk = 36
        val envCode = System.getenv("ASCENDY_VERSION_CODE")?.toIntOrNull()
        versionCode = envCode ?: 30
        versionName = "0.3.${versionCode}"
        buildConfigField("String", "RELEASE_REPO", "\"bandersong/ascendy\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ───── Distribution flavors ─────
    // foss: keeps the in-app GitHub-Releases updater + REQUEST_INSTALL_PACKAGES.
    // play: stripped — no in-app installer; updates flow through Play. Different
    // applicationId so it can coexist with the foss sideload build, and because
    // com.ascendy.app is reserved on the Play Store.
    flavorDimensions += "distribution"
    productFlavors {
        create("foss") {
            dimension = "distribution"
            buildConfigField("Boolean", "HAS_INAPP_UPDATER", "true")
        }
        create("play") {
            dimension = "distribution"
            applicationId = "io.github.bandersong.ascendy"
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

        // Release config — created ONLY when CI has provided the real signing secrets. There is
        // deliberately no debug fallback: the debug keystore is committed to this repo, so a
        // debug-signed "release" could be impersonated by anyone and the in-app updater's signature
        // pin would (correctly) reject it. When the secrets are absent, release builds fail (see the
        // guard at the bottom of this file) rather than producing a publicly-keyed APK.
        if (hasReleaseSigning) {
            create("release") {
                val ksFile = layout.buildDirectory.file("generated-keystore/release.keystore").get().asFile
                ksFile.parentFile.mkdirs()
                ksFile.writeBytes(Base64.getDecoder().decode(releaseKeystoreB64))
                storeFile = ksFile
                storePassword = releaseStorePwd
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPwd
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            // Side-by-side installs: `-PidSuffix=.test` gives the debug build its own
            // applicationId so it can sit next to a release install on the same device
            // (release builds are CI-signed, so they can never be updated in place by a
            // debug build — without this the only route is uninstall, which wipes data).
            (findProperty("idSuffix") as String?)?.let {
                applicationIdSuffix = it
                versionNameSuffix = it
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Sign release with the real CI key only. If it's missing, leave the build unsigned so the
            // guard task aborts it — never debug-sign a release (that key is public in the repo).
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true   // Robolectric needs merged resources/manifest
            isReturnDefaultValues = true        // stub un-shadowed android.* calls instead of throwing
        }
    }

    // Room exports the schema JSON here; MigrationTest reads it from androidTest assets to build
    // and validate the on-disk schema. Commit app/schemas/ so historical versions stay reproducible.
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Safety net: never emit a release artifact without the real signing key. When the CI secrets are
// absent, the release buildType has no signingConfig (see above), so AGP would otherwise produce an
// UNSIGNED release. Abort the packaging/signing tasks instead, with a message pointing at the fix.
// Debug tasks (incl. `compilePlayDebugKotlin`) are untouched — this only fires for release packaging.
if (!hasReleaseSigning) {
    tasks.configureEach {
        val n = name
        val isReleaseArtifact = n.contains("Release") && !n.contains("Test") &&
            (n.startsWith("package") || n.startsWith("sign"))
        if (isReleaseArtifact) {
            doFirst {
                throw GradleException(
                    "Refusing to build a release without the real signing key. Set " +
                    "ASCENDY_RELEASE_STORE_PASSWORD, ASCENDY_RELEASE_KEY_PASSWORD, " +
                    "ASCENDY_RELEASE_KEY_ALIAS and ASCENDY_RELEASE_KEYSTORE_BASE64 (the CI signing " +
                    "secrets) before assembling a release. The debug keystore is public in this repo " +
                    "and must never sign a release."
                )
            }
        }
    }
}

dependencies {
    // 2025.06.01 is the last BOM on the Compose 1.8 line — newer (1.9+) requires Kotlin 2.1.
    val composeBom = platform("androidx.compose:compose-bom:2025.06.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
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

    // ── JVM unit tests (fast, no device) — run via :app:testFossDebugUnitTest ──
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("reflect"))   // VocabTest walks Vocab fields reflectively
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.test.ext:junit-ktx:1.2.1")

    // ── Screenshot tests (Roborazzi on Robolectric, no emulator) ──
    // Record/verify the design-system gallery across 3 themes x light/dark:
    //   ./gradlew :app:recordRoborazziFossDebug   (writes goldens)
    //   ./gradlew :app:verifyRoborazziFossDebug    (CI gate vs goldens)
    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.43.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.43.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:1.43.1")

    // ── Instrumented tests (emulator) — run via :app:connectedFossDebugAndroidTest ──
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
