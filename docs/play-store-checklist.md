# Play Store launch checklist

Everything you paste into Play Console, plus the steps that need to happen on your phone / in a browser.

> **Status (2026-06-11):** Steps 1–5 DONE — Pages live, signing secrets set, signed
> `app-play-release.aab` (R8-minified, CN=bandersong release cert, `jarsigner` verified) builds
> green via workflow_dispatch. Play Console account exists (Ascendtech), app is a draft
> (`io.github.bandersong.ascendy`) with **internal testing active** (build 54 on the track) and
> Play App Signing ENABLED. Remaining for production: finish store setup (~3 items of 13),
> listing copy below, screenshots ×6, feature graphic, 512 icon, then promote.
>
> ⚠️ **Keystore backup:** the upload keystore exists ONLY as the
> `ASCENDY_RELEASE_KEYSTORE_BASE64` GitHub secret — secrets can't be read back out and no `.jks`
> was found on the Mac. Play App Signing is on, so a lost upload key is rotatable via Play
> Console support, but that's a support ticket and days of friction. Back the `.jks` up properly
> (1Password) or regenerate + re-set the four secrets while it's cheap.

## 1. Enable GitHub Pages (1 min)

So the privacy-policy URL works:

1. Open https://github.com/bandersong/ascendy/settings/pages
2. **Source**: Deploy from a branch
3. **Branch**: `main`
4. **Folder**: `/docs`
5. Save. Wait ~1 minute.
6. Verify: https://bandersong.github.io/ascendy/privacy.html should load.

This is the URL you paste into Play Console's "Privacy policy URL" field.

## 2. Generate the release keystore (10 min)

You only do this once. **Keep the resulting `.jks` file safe** — losing it means you can never update the Play Store app again (unless you've set up Play App Signing, which we recommend below).

Run on your Mac:

```bash
KEYTOOL="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool"
"$KEYTOOL" -genkey -v \
  -keystore ~/ascendy-release.jks \
  -storepass <CHOOSE_A_STRONG_PASSWORD> \
  -keypass    <CHOOSE_THE_SAME_OR_DIFFERENT> \
  -keyalg RSA -keysize 2048 \
  -validity 25000 \
  -alias ascendy \
  -dname "CN=bandersong, O=Ascendy, C=US"
```

Then base64-encode it for the GitHub secret:

```bash
base64 -i ~/ascendy-release.jks -o ~/ascendy-release.b64
```

Open `~/ascendy-release.b64` and copy its full contents.

**Store `~/ascendy-release.jks` somewhere safe** (1Password, encrypted backup, etc.). Do NOT commit it to the repo. The `.gitignore` already excludes `.jks` files but be careful.

## 3. Add four GitHub secrets

https://github.com/bandersong/ascendy/settings/secrets/actions

| Name | Value |
|---|---|
| `ASCENDY_RELEASE_STORE_PASSWORD` | your store password |
| `ASCENDY_RELEASE_KEY_PASSWORD` | your key password (same, unless you set them different) |
| `ASCENDY_RELEASE_KEY_ALIAS` | `ascendy` |
| `ASCENDY_RELEASE_KEYSTORE_BASE64` | paste the entire contents of `~/ascendy-release.b64` |

## 4. Build a signed AAB

https://github.com/bandersong/ascendy/actions/workflows/build.yml → **Run workflow** → check **Also build Play (release AAB)** → Run.

When done, download the `ascendy-play-aab` artifact. Inside is `app-play-release.aab` — that's what you upload to Play Console.

## 5. Create the Play Console account ($25)

https://play.google.com/console — one-time $25 fee. Use a personal account if it's your app, or set up an Organization if you want company branding.

When asked to set up Play App Signing: **say yes**. Google holds the master key, you keep the upload key. If you lose your upload key, you can rotate it. If you do self-managed signing and lose the key, the app is dead forever.

## 6. Create the app listing

In Play Console → **Create app** → fill in:

- App name: **Ascendy**
- Default language: English (United States)
- App or game: App
- Free or paid: Free (charge later via IAP if you want)
- Declarations: tick all the boxes that apply truthfully.

## 7. Listing copy (paste-ready)

### Short description (max 80 chars)

```
Tap an NFC tag or QR code to start a focus session. Block apps + websites.
```

### Full description (max 4000 chars)

```
Tap a physical anchor — a small NFC sticker or a printed QR code — to start a focus session. Distracting apps and websites get blocked until you tap the anchor again. The friction of having to physically reach the tag is the whole point.

The same idea behind Brick, but open source and customizable.

WHAT GETS BLOCKED
• Any apps you choose — pick from your installed app list
• Any websites you list — domains are blocked at the network layer (DNS sinkhole) plus URL-bar detection in 15 browsers (Chrome, Firefox, Brave, Edge, Samsung Internet, DuckDuckGo, Opera, Vivaldi, Tor and more)
• Bounce-back is instant: try to open a blocked app and Ascendy returns you to home

HOW YOU UNLOCK
• Tap the same NFC tag you started with
• Or scan your QR code with the in-app camera
• Or use a one-time emergency override — but you have to type a verbatim sentence first (the "friction tax")
• Or wait for the safety timer you set (default 8h, max 24h)
• In strict mode, the override is disabled — only the anchor or the timer

EXTRA TOOLS
• Pomodoro sessions: 15 / 25 / 50 / 90 minute timers, auto-end
• Scheduled focus: lock automatically at specific times on chosen days
• Allow-only mode: invert the list — only listed apps work, everything else is blocked
• Per-tag bindings: each anchor can trigger its own focus list
• Daily focus goal: set a target, mascot reacts when you hit it
• Stats: today, week, all-time, 7-day chart, current streak
• Streak rewards: at 7 days your mascot earns a headband, at 30 days a sparkle, at 100 days a crown

THREE THEMES, FULLY THEMED
• Kawaii: strawberry-milk pink, smiling mascot, soft copy
• Tough: iron + chains, scowling mascot, no-nonsense copy
• Neutral: corporate sentence case, clean palette
Switch any time from Settings.

PRIVACY
Ascendy collects no personal data. No analytics, no crash reporting, no third-party SDKs. All blocking happens on-device. The optional VPN-based website blocking runs a local DNS sinkhole — no traffic leaves your phone except standard DNS queries to a public resolver.

Full privacy policy: https://bandersong.github.io/ascendy/privacy.html
Source code: https://github.com/bandersong/ascendy
```

## 8. Privacy policy URL

**`https://bandersong.github.io/ascendy/privacy.html`** — paste into Play Console → App content → Privacy policy.

## 9. Data Safety form

**Does your app collect or share any of the required user data types?** → **No**

That's the whole thing. Every subcategory: no.

If asked about "data processed ephemerally" — only DNS queries during a focus session (forwarded to a public resolver, not stored or shared). You can disclose this if you want to be thorough, but it's optional under Play's definitions because we don't transmit it anywhere we control.

## 10. Permission Declaration justifications

In Play Console → App content → Permissions declaration. For each sensitive permission, paste the matching block.

### Accessibility Service (BIND_ACCESSIBILITY_SERVICE)

**Use case category**: Health & Wellness — Digital Wellbeing

**Justification**:
```
Ascendy is an opt-in focus / digital-wellbeing app. When the user starts a focus session (by tapping an NFC tag or scanning a QR code they've paired), the accessibility service detects which app is in the foreground and which website is loaded in the URL bar of supported browsers. If either matches a user-configured block list, Ascendy returns the user to the home screen. The service runs only while a session is active and inspects only the currently-active window. It does not log, persist, or transmit any content. Before the user is directed to enable the service, the app shows a prominent in-app disclosure describing exactly what is read (foreground app name, browser address bar), when (active sessions only), and that nothing is stored or transmitted; the user must explicitly consent. This is the same mechanism used by published apps including BlockSite, AppBlock, StayFree, ScreenZen, and One Sec for the same user-initiated digital-wellness purpose.
```

**Prominent disclosure**: implemented in-app — the Permissions screen shows a consent dialog (see `a11yDisclosureBody` in Vocab.kt) before opening Accessibility Settings the first time. Mention this in the declaration's free-text field.

### QUERY_ALL_PACKAGES

**Not requested.** The app enumerates launchable apps via a `<queries>` intent declaration (MAIN/LAUNCHER) instead of broad package visibility, so no QUERY_ALL_PACKAGES declaration is needed. If Play Console still surfaces the form, answer "my app does not request this permission."

### Device admin (Lockdown mode, anti-uninstall)

If the reviewer asks about the device-admin usage (`AscendyDeviceAdminReceiver`), paste:
```
Lockdown mode is a strictly opt-in commitment feature for digital wellbeing: while enabled, Ascendy cannot be uninstalled mid-focus-session, closing the obvious bypass of deleting the blocker to reach a blocked app. The user must explicitly enable it in Settings behind a confirmation dialog that explains the device-admin activation, and Android's own device-admin consent screen follows. Device admin is used ONLY to block uninstallation — no other admin policies are used. Lockdown can be disabled by the user at any time when no focus session is active, and every session is guaranteed to end via a mandatory safety timer (max 24h).
```

### SCHEDULE_EXACT_ALARM (USE_EXACT_ALARM removed in build 66)

Play Console's exact-alarm declaration only allows USE_EXACT_ALARM for apps whose core
functionality is "alarm clock" or "calendar" — a focus blocker is neither, so the permission was
removed from the manifest (build 66+). SCHEDULE_EXACT_ALARM remains: the user grants
"Alarms & reminders" access, and `AlarmScheduler` already falls back to inexact alarms when
ungranted. No Play declaration form gates SCHEDULE_EXACT_ALARM.

**Justification (if ever asked)**:
```
Ascendy supports user-defined scheduled focus sessions (e.g., "block social media weekdays 9am-12pm") and pomodoro-style timed sessions. Both require precise wall-clock timing to start and end sessions at the exact times the user configured. AlarmManager.setExactAndAllowWhileIdle is the standard API for this; inexact alarms can drift by tens of minutes, which would break the schedule semantics the user relies on.
```

### REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

**Justification**:
```
The user can optionally exempt Ascendy from battery optimization to keep the blocking enforcement reliable during long focus sessions on aggressive OEM platforms (Samsung One UI, Xiaomi MIUI, OnePlus OxygenOS). The exemption is requested only when the user taps "Exempt Ascendy" on the Permissions screen, never silently.
```

### SYSTEM_ALERT_WINDOW

**Justification**:
```
Used to display the focus-session overlay screen when a blocked app is opened. The overlay appears only while a session is active and immediately after a block event.
```

### POST_NOTIFICATIONS

Not sensitive — standard runtime permission on Android 13+. No declaration form needed.

## 11. App content — content rating

Run the IARC questionnaire honestly. Ascendy contains no objectionable content, no ads, no IAP (yet), no UGC. Expected rating: Everyone / 3+.

## 12. Screenshots (you take these on your phone)

Play Console requires 2–8 phone screenshots. Recommended set of 6:

1. **Home, Kawaii theme, idle** — mascot, ready badge, daily-goal progress, tools row
2. **Home, Tough theme, locked-in session** — angry mascot with chains, timer, blocked count
3. **Home, Neutral theme, post-session** — clean look, "all set" + streak chip
4. **Settings screen** — shows the three theme cards with live mascot previews
5. **Stats screen** — 7-day chart + recent sessions
6. **AppPicker on the Sites tab** — domain list, "add" button

Optional 7th if you want one: **Permissions screen** — shows the legit perm flow.

Frame each at 1080×1920 or higher, portrait. Take with the phone, no special tooling needed.

## 13. Feature graphic (1024×500)

A single horizontal banner. Suggestion: dark background, the mascot from each theme side-by-side, "ascendy ♡" wordmark to the left. Figma file recommended. You can also just take a screenshot of a phone showing the kawaii home and crop.

## 14. App icon (512×512)

A higher-resolution version of the launcher icon. Same vector star + dot-eye, exported at 512×512. Or design new in Figma.

## 15. Submit to internal testing first

In Play Console → Internal testing → Create a new release → upload the AAB → list yourself as a tester → install via the Play Store link → smoke test. Fix anything obvious. THEN promote to production.

For first submission of an app using accessibility + foreground services, expect 1-7 days of manual review.
