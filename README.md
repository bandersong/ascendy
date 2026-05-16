# ascendy ♡

An NFC- and QR-based focus app for Android. Pair a physical anchor — tap it to lock distracting apps and websites, tap it again to unlock. The friction is the feature.

Three themes (Kawaii, Tough, Neutral) — the whole app rewrites itself: colors, typography, mascot, copy, even the foreground-service notification.

[Privacy policy](https://bandersong.github.io/ascendy/privacy.html) · [Latest APK](https://github.com/bandersong/ascendy/releases/latest) · MIT licensed · nothing leaves your device.

## What it does

- **NFC anchors** — write a UUID to any NTAG21x sticker. Tap to start a focus session. Tap the same tag to end it.
- **QR anchors** — generate a printable QR code, stick it somewhere inconvenient, scan it with the in-app camera to toggle a session.
- **App blocking** — pick installed apps to block. The accessibility service bounces you back to home if you try to open one during a session. Fallback path uses UsageStats polling.
- **Website blocking** — two layers stacked:
  - Accessibility service reads the URL bar in 15 known browsers and bounces you out
  - VpnService DNS sinkhole answers blocked domains with NXDOMAIN at the network layer (on-device only, optional)
- **Pomodoro** — timed sessions that auto-end (15 / 25 / 50 / 90 min)
- **Scheduled focus** — daily sessions on chosen days, AlarmManager-driven
- **Per-tag list bindings** — each anchor can trigger its own focus list
- **Strict mode** — per-list. No emergency override, no manual end. Only the bound anchor or the safety timer.
- **Friction-tax override** — for non-strict lists, the emergency unlock requires typing a verbatim sentence (case-sensitive, theme-specific)
- **Forced safety timer** — every session auto-ends after a user-set max duration (1h–24h, default 8h). Fail-safe in case you lose your anchor.
- **Allow-only mode** — invert the block list semantics. Only the listed apps work. Great for study-only sessions.
- **Stats + 7-day chart + streaks** — earned mascot accessories at 7 / 30 / 100 day streaks.
- **Daily focus goal** — pick a daily target; progress shown on home.
- **Three themes** with full vocabulary swaps (every string is per-theme).
- **Home-screen widget** — status, scan button, streak chip. Works on the lock screen on Android 13+.
- **Quick Settings tile** — pull down the shade, tap to toggle a manual session.
- **Notification actions** — End / Stats inline from the ongoing-session notification.
- **Tasker broadcast intents** — `com.ascendy.app.SESSION_STARTED` / `SESSION_ENDED` for automation.
- **In-app updater** — pulls the latest build from GitHub Releases. No more downloading artifacts from the Actions tab.

## Get it

- **Sideload:** download [the latest APK](https://github.com/bandersong/ascendy/releases/latest) and tap to install.
- **F-Droid:** coming soon.
- **Google Play:** coming soon.

After installing the first time, future updates install over the top — the debug keystore is stable and committed to the repo so signing matches across builds.

## Build it yourself

CI builds the `foss` flavor on every push to `main`. Locally with Android Studio:

```bash
./gradlew :app:assembleFossDebug      # default sideload build (with in-app updater)
./gradlew :app:bundlePlayRelease      # Play Store AAB (no in-app installer)
```

`compileSdk` / `targetSdk` 35 · `minSdk` 26 · Kotlin 1.9.24 · Compose BOM 2024.06.00 · Room 2.6.1.

## Architecture

```
                    ┌──────────────┐
   NFC tag tap ──►  │ MainActivity │  reads tag id, calls SessionController
   QR scan     ──►  └──────┬───────┘
                           ▼
                    ┌──────────────────────┐
                    │ SessionController    │  state machine: lock/unlock
                    └──────┬───────────────┘
                           ▼
                    ┌──────────────────────┐         ┌──────────────────┐
                    │ Room DB (session)    │ ──────► │ BlockState cache │
                    └──────────────────────┘         └────────┬─────────┘
                                                              ▼
        ┌──────────────────┐    ┌─────────────┐    ┌──────────────────┐
        │ Accessibility    │    │ Foreground  │    │ VpnService DNS   │
        │ Service          │    │ Service     │    │ sinkhole         │
        │ (primary)        │    │ (fallback)  │    │ (websites)       │
        │                  │    │ UsageStats  │    │                  │
        └────────┬─────────┘    └──────┬──────┘    └──────────────────┘
                 │                      │
                 ▼                      ▼
              foreground app ∈ blockset → home + blocker overlay
              URL bar host ∈ domain set → home + blocker overlay
```

`BlockState` is the in-memory hot cache so the accessibility service doesn't hit Room on every window event.

## Privacy

Ascendy collects no personal data. All app and website blocking happens entirely on your device. Nothing is transmitted, shared, sold, or stored on any server we control. No analytics. No crash reporting. No third-party SDKs.

The two outbound network calls Ascendy makes:
- DNS forwarding during a focus session (only routing standard DNS queries to a public resolver like 1.1.1.1)
- The optional in-app updater fetching release metadata + the APK file from GitHub when you tap "Check for updates"

Full per-permission breakdown at the [privacy policy](https://bandersong.github.io/ascendy/privacy.html).

## License

MIT — see [LICENSE](LICENSE).
