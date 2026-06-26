plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
    // Screenshot testing (JVM, via Robolectric — no emulator). See docs/UI_MASTERPIECE.md.
    id("io.github.takahirom.roborazzi") version "1.43.1" apply false
}
