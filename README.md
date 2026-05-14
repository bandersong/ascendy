# ascendy ♡

Open-source NFC focus app for Android. Tap a paired tag to lock distracting apps; tap the same tag to unlock.

## Build (phone-only flow)

1. Push this repo to GitHub.
2. The `Build APK` workflow runs on every push to `main` (and via "Run workflow" on the Actions tab).
3. Download `ascendy-debug-apk` from the run artifacts on your phone.
4. Tap the APK to install (allow "install unknown apps" for your file manager / browser if prompted).

## First-run setup

1. Open ascendy.
2. Walk through **permissions** — grant accessibility, usage access, display-over-other-apps, and notifications.
3. **Pair a tag** — tap "start pairing", hold a blank NTAG21x against the back of the phone, give it a name.
4. **Build a focus list** — name a list, then toggle the apps you want to block.
5. Tap the tag from anywhere → focus on → blocked apps show the kawaii overlay.
6. Tap the same tag → focus off.

## Architecture

- `MainActivity` routes NFC intents and hosts the Compose nav graph.
- `SessionController` is the state machine: start session, end session, emergency unlock, restore-on-boot.
- `BlockingAccessibilityService` is the primary detector — listens to window-state-changed events and bounces back to home.
- `BlockingForegroundService` is the fallback — polls `UsageStatsManager.queryEvents` every 700 ms. Required when accessibility is disabled (e.g. Android 17 Advanced Protection Mode).
- `BlockState` is the in-memory cache so the accessibility service doesn't hit Room on every window event.
- `BootReceiver` restores state after reboot.

## Known limitations

- A blocked app may render one frame before the home action fires. Acceptable for v1.
- Android 17 AAPM disables the accessibility path; only the usage-stats fallback runs. Documented on the permissions screen.
- No website blocking (would need `VpnService`).
- No uninstall protection (would need Device Admin opt-in).
