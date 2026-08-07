# AUTOLOOP STATE — Ascendy vs Brick

Campaign: AAAA quality, sellable ≥$5. Started 2026-08-07.

## Target
- Repo: ~/ascendy, branch base = `origin/feat/ui-design-tokens` (PR #5, 14 ahead 0 behind main — KEYSTONE, unmerged; all campaign PRs stack on it, base = feat/ui-design-tokens)
- Campaign branch: `autoloop/campaign` (state + refs live here; dimension branches fork CLEAN from origin/feat/ui-design-tokens)
- Build: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:assembleFossDebug` → grep BUILD (never trust tail)
- versionCode local=30 < CI=69 → `adb -e uninstall com.ascendy.app` before installing local over CI build

## Harness (rail-compliant, nothing on desktop)
- Headless emulator: `ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools /opt/homebrew/share/android-commandlinetools/emulator/emulator -avd ascendy_test -no-window -no-audio -no-boot-anim -no-snapshot-save -gpu swiftshader_indirect`
- adb: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb` → emulator-5554, 1080x2400@420, Android 14
- Screenshot: `adb -e exec-out screencap -p > file.png`; UI tree: `adb -e shell uiautomator dump`
- ONE emulator → critics/verifiers serialize. NFC untestable on emulator (QR + manual + unit tests only).
- SECOND lane: `hbtest` AVD → emulator-5556 (Android 15, 1080x2400@420). Boot with `ANDROID_SDK_ROOT=~/Library/Android/sdk ~/Library/Android/sdk/emulator/emulator -avd hbtest -port 5556 ...` (its android-35 image lives in the OTHER sdk root — ascendy_test uses homebrew root, hbtest uses ~/Library/Android/sdk).
- THIRD lane (FASTEST, real GPU): `jesus` Linux box, AVD `ascendy_x86` (Android 14, x86_64, 1080x2400@420) → Mac adb serial **`localhost:5686`** via ssh forward. Full runbook: `.autoloop/linux-lane/README.md`. Verified from Mac: cold launch **626ms** vs 888ms Mac-swiftshader.
  - SELinux Enforcing on Bazzite kills ALL software rasterizers (`avc denied { execheap }` → SIGSEGV mid-boot). Lane runs `-gpu host` on the RX 6800 XT borrowing the KDE X cookie — needs jesus's desktop session up. Sudo-fix if ever needed: `setsebool -P selinuxuser_execheap on`.
  - Forward ports 5685/5686 chosen ABOVE adb's 5554-5585 auto-scan so the Mac server never probes the busy lanes. Never `adb kill-server` on the Mac — it would drop 5554/5556.

## References
- Brick (Brick LLC) — Play `com.brickllc.brick`, 4.9★ 3.6K reviews, 100K+ DLs. $59 device + free app.
- Ref screenshots: `.autoloop/refs/brick/play_shot_{1..5}.png` (1772x3150). Site: getbrick.com.
- Brick moat: "no overrides or workarounds"; minimal-zen visual, huge type, calm neutrals, product-render hero.
- Speed refs (can't install Brick app — no device/hardware): use absolute-feel bars: cold launch <1s, transition <300ms, zero jank.

## Baseline (r0, campaign build, emulator)
- Cold launch TotalTime: 888ms (am start -W, swiftshader emu — relative measure only)
- First-run screenshot: `.autoloop/baseline/r0_first_run.png` (kawaii onboarding)

## Dimensions
| dimension | status | rounds | last verdict | PR |
|---|---|---|---|---|
| visual-design (3 themes × spacing/type/consistency) | OPEN | 0 | — | — |
| core-workflow (session start/end, pair, click count) | OPEN | 0 | — | — |
| states (empty/error/edge/permission-denied) | OPEN | 0 | — | — |
| copy (3-theme vocab, tone, spelling) | OPEN | 0 | — | — |
| onboarding (first-run, permission education) | OPEN | 0 | — | — |
| speed (cold launch, transitions, jank) | OPEN | 0 | — | — |
| dark-light (theme × mode matrix) | OPEN | 0 | — | — |
| accessibility (TalkBack, contrast, targets, font-scale) | OPEN | 0 | — | — |
| correctness (blocking works: app/web/DNS, safety timer, strict) | OPEN | 0 | — | — |
| bypass-resistance (the CORE PROMISE — red-team) | OPEN | 0 | — | — |
| resilience (crash, process-death, Room, OEM kill) | OPEN | 0 | — | — |
| settings-help (discoverability, in-app help) | OPEN | 0 | — | — |
| widget-tile-notif (widget, QS tile, notification) | OPEN | 0 | — | — |

Budget: 8 rounds/dimension. Rotation: user-facing first.

**bypass-resistance is the make-or-break dimension.** Brick's entire $59 moat is "no overrides or workarounds". If a phone-only relapsing user can defeat Ascendy, no amount of visual polish makes it sellable. Red-team runs on the Linux lane (localhost:5686), 4 adversary lenses × loop-until-dry, every claim verified by 3 independent refuters (majority-refute kills it). Findings ranked phone-only above needs-computer.

## Risk flags (flagged at planning)
1. PR #5 = keystone; user should merge when happy or campaign rebases later.
2. Emulator-only: NFC paths + real-device speed unverifiable here. Correctness dim scoped accordingly.
3. Refs = Play marketing shots (framed/styled) — critics compare UI regions, not frames.
4. MIT repo, screenshot refs comparison-only → no license taint.

## Round log
### r0 (recon, 2026-08-07)
Phase 0: harness proven (headless emu + adb), fossDebug builds green, APK installed, Brick ref pack cached, baseline shot + 888ms cold launch. Round 1 = critic-first audit of top-4 user-facing dims (visual-design, core-workflow, states, copy) — produces ranked defect lists; builders start r2.

### r1 scale-up (ultracode, same day)
User: token-max, full send. Booted second emulator (hbtest→5556, Android 15). Two workflows in flight: r1 = 4 critics on 5554 (core-workflow, visual-design, states, copy); r1b = 8 remaining dims (copy-static, a11y-static, correctness-code→resilience-code static lane; onboarding→dark-light→a11y-dynamic→speed→settings-widget on 5556) + Brick ref harvester (site + iOS shots). ALL 12 dims audited in r1. Builders act r2 on ranked defect lists.
