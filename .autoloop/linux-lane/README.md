# Linux test lane (`jesus`) — x86_64 Android emulator

Fast KVM-accelerated Android 34 emulator on the Linux desktop, driven from this
Mac over an SSH port-forward. ~600–680 ms cold launch for Ascendy vs. the
swiftshader ARM emulators here.

Host: `jesus@100.111.196.51` (Tailscale) — Bazzite, i9-10900K (20 cores),
31 GB RAM, Radeon RX 6800 XT, `/dev/kvm` writable by the user.

## adb serial

```
localhost:5686
```

Use it explicitly on every command — the Mac's `emulator-5554` /
`emulator-5556` are separate lanes and must not be disturbed.

```sh
ADB=/opt/homebrew/share/android-commandlinetools/platform-tools/adb
$ADB -s localhost:5686 shell am start -W -n com.ascendy.app/.MainActivity
$ADB -s localhost:5686 exec-out screencap -p > shot.png
```

## (a) Re-establish the SSH port-forward (run on the Mac)

```sh
ssh -f -N -o ExitOnForwardFailure=yes \
  -L 5685:localhost:5554 -L 5686:localhost:5555 jesus@100.111.196.51
/opt/homebrew/share/android-commandlinetools/platform-tools/adb connect localhost:5686
```

`5685` → emulator console (5554), `5686` → emulator adb (5555). Both are
deliberately **above** adb's 5554–5585 auto-scan range so the Mac's adb server
never probes them and never touches the two local emulators.

Tear down: `pkill -f 'ssh -f -N .* -L 5686'` and
`adb disconnect localhost:5686`.

## (b) Boot the emulator on jesus

```sh
ssh jesus@100.111.196.51 'bash ~/boot-ascendy-emu.sh'
```

Detached via `setsid`, survives the SSH session ending. Log: `~/emu.log` on
jesus. Boots to `sys.boot_completed=1` in ~30 s. Poll it:

```sh
ssh jesus@100.111.196.51 'source ~/android-env.sh; adb -s emulator-5554 shell getprop sys.boot_completed'
```

…but see the adb-server gotcha below before running adb on jesus.

## (c) Install a fresh APK

```sh
scp app/build/outputs/apk/foss/debug/app-foss-debug.apk jesus@100.111.196.51:~/ascendy.apk
/opt/homebrew/share/android-commandlinetools/platform-tools/adb -s localhost:5686 install -r \
  app/build/outputs/apk/foss/debug/app-foss-debug.apk
```

(Installing straight over the forward works fine; the `scp` is only useful if
you want to install locally on jesus.)

## What's installed on jesus

| Thing | Path |
|---|---|
| JDK 21 (Temurin) | `~/jdk` |
| Android SDK | `~/android-sdk` (platform-tools 37.0.1, emulator 37.1.11, android-34 + `google_apis;x86_64`) |
| Env file | `~/android-env.sh` — `source` it for `adb`/`emulator`/`sdkmanager` on PATH |
| Boot script | `~/boot-ascendy-emu.sh` |
| AVD | `ascendy_x86` — 1080×2400 @ 420 dpi, 4 GB RAM, 6 GB data, 6 cores |

## Gotchas

**SELinux kills software rendering.** SELinux is Enforcing and
`selinuxuser_execheap` is `off`, so any software rasteriser — SwiftShader *or*
mesa lavapipe — gets `avc denied { execheap }` on its shader-JIT thread and the
emulator SIGSEGVs mid-boot. `-gpu swiftshader_indirect`, `-gpu guest` and
`-gpu off` all die this way. The lane therefore runs `-gpu host` on the real
RX 6800 XT. Confirm a crash with
`ssh jesus 'journalctl -q --since "-5min" | grep execheap'`.

The clean alternative (needs the sudo password, which nobody was around to
type): `sudo setsebool -P selinuxuser_execheap on`.

**`-gpu host` needs the desktop session's X cookie.** The box runs a KDE
Wayland session on tty2 with Xwayland. `boot-ascendy-emu.sh` sets
`DISPLAY=:0` and picks up `XAUTHORITY` from `/run/user/1000/xauth_*`. Without
the cookie you get `Failed to get EGL display`. If jesus is ever rebooted to a
console with no graphical session, this lane won't boot — flip the SELinux
boolean above and switch `EMU_GPU=swiftshader_indirect` instead.
`-no-window` means nothing appears on his physical screen either way.

**Don't run `adb` on jesus while the Mac is connected.** The emulator accepts
exactly one adb server on port 5555. jesus's local adb server was killed
(`adb kill-server`) so the Mac could own it. Any `adb` command on jesus
restarts a server there and steals the connection — the Mac then shows
`localhost:5686 offline`. Fix: `ssh jesus 'source ~/android-env.sh; adb kill-server'`
then `adb connect localhost:5686` again from the Mac.

**Stale qemu ignores SIGTERM.** A failed-renderer emulator hangs onto the AVD
lock and reports "Another emulator instance is running". The boot script
already `pkill -9`s it first. Never write `pkill -f 'emulator -avd …'` inside
an `ssh '…'` one-liner — the pattern matches the SSH session's own command line
and kills your connection.

**APK ABI.** `app-foss-debug.apk` ships `lib/x86_64/` (plus x86, arm64-v8a,
armeabi-v7a), so it runs natively here — no ARM translation involved.

## Measured

Cold launch, `am start -W -n com.ascendy.app/.MainActivity`, after
`pm clear`, driven from the Mac over the forward:

```
Status: ok
LaunchState: COLD
TotalTime: 604
WaitTime: 607
```

(681 ms on the first run directly on jesus.) Smoke screenshot: `smoke.png`
— onboarding page 1, 1080×2400.
