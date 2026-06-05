# Testing

Ascendy has two test layers. CI runs both ([.github/workflows/test.yml](../.github/workflows/test.yml)); you can run them locally too.

## Layers

| Layer | Location | Needs | Runs |
|-------|----------|-------|------|
| Unit + Robolectric | `app/src/test` | JDK only | `:app:testFossDebugUnitTest` |
| Instrumented | `app/src/androidTest` | emulator/device | `:app:connectedFossDebugAndroidTest` |

**Unit / Robolectric** — fast, no device. Pure logic (`DnsTools`, `Stats`, `Domains`, `UrlHost`, `AlarmScheduler.nextFiringFrom`, `Updater`, `QrTools`), the `BlockState` block/allow predicates, the `Vocab` three-theme format-string parity check, and the `SessionController` state machine + `AscendyRepo` over Robolectric.

**Instrumented** — real Android. App launch + first Compose frame, Room round-trip, and the Room **migration** test (builds a v6 DB, runs the real migration, validates against the exported schema, asserts no data loss).

## Run locally

Requires a JDK 17 and the Android SDK. Example env (Homebrew layout):

```sh
export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Fast layer — no device
./gradlew :app:testFossDebugUnitTest

# Instrumented — boot an emulator first (any AVD), then:
./gradlew :app:connectedFossDebugAndroidTest
```

CI runs the instrumented layer on an emulator matrix (API 26 / 30 / 34); it's skipped on PRs and runs on pushes to `main`.

## Schema migrations

`exportSchema = true` writes JSON to `app/schemas/` — **commit it** on every DB version bump.
When bumping `AscendyDb.version`, add a real `MIGRATION_<old>_<new>` with the ALTER/CREATE SQL,
register it in `.addMigrations(...)`, and extend [MigrationTest](../app/src/androidTest/java/com/ascendy/app/data/MigrationTest.kt)
to validate the new path. An empty migration body on a real schema change makes Room throw on
open — that's intentional (fail-loud, never a silent wipe).
