# Ascend Ascendy — idea backlog

Durable, deduplicated, prioritized backlog of ways to grow Ascendy. The autonomous
"/loop 20m" ideation fire reads this, adds **genuinely new** grounded ideas (dedup hard),
re-ranks, and spikes the cheap+verifiable ones. Not a brain-dump — a curated menu.

**Grounding (don't invent blind):** Ascendy is a native Android (+iOS WIP) focus/blocker.
Moats already in hand: **physical NFC/QR anchors**, **offline + no-account privacy** (MIT,
"nothing leaves your device"), **3 full-identity themes** (color+type+mascot+copy+vocab),
**deep enforcement** (a11y bounce + VPN DNS sinkhole + device-admin Lockdown + strict mode).
Audience: people seeking calm/relief, one-time purchase, anti-subscription, privacy-minded.
Competitors: Brick (NFC hardware tile), one sec (breath pause), Opal (subscription), Forest (trees).

**Brand guardrails (the red-team axis):**
- ✅ relief / empowerment / calm — ❌ never fear / shame / guilt ([[feedback_marketing_tone_relief_not_fear]])
- ✅ offline, on-device, no account — ❌ nothing that needs a server or leaks data
- ✅ one-time purchase / tip-jar — ❌ subscriptions
- ✅ permission-minimal — ❌ creepy permissions (location, contacts) unless huge + local-only

Scoring: **Impact** (H/M/L) × **Effort** (S/M/L). `★` = top picks. `⚠️` = brand/privacy tension.

---

## ★ Top picks (highest leverage, on-brand)

| # | Idea | Impact | Effort | Why it's leverage |
|---|------|:--:|:--:|---|
| 1 | **Peaceful streaks — pause, don't reset.** Miss a day → streak *pauses* (plant stops growing), never zeroes. Optional "grace day" bank. **Plus a totals-only / "no streak" mode** for users who find any streak stressful. | H | S | The single biggest brand-fit: captures the exact users who fail a streak, feel shame, and delete. Pure local date logic → **unit-testable**. *(red-team: a streak can still pressure — hence the opt-out to pure totals; relief means the metric is never a stick.)* |
| 2 | **Focus Contracts — shareable block configs via QR.** Encode a list (apps+domains+strict flags) into a printable/screenshot QR; scanning imports it instantly. | H | M | Turns a creator's "Writing Weekend" setup into a scan-to-install. Zero account/cloud — leans on the QR moat. Power users → evangelists. |
| 3 | **Blocklist export/import files (.ascendylist).** First-class: *you* ship a few curated starter packs ("doomscroll / news / shopping"); users export/import + share. | M | S | Opal-tier lists without betraying offline/no-account. *(red-team downgrade: a "community" of randoms = broken domains + support load + adversarial tone. Keep it first-party-curated + personal-share, NOT a crowd repo.)* |
| 4 | **Companion packs (one-time IAP) — mascot + sound + theme skins.** Core stays 100% free; $2–4 aesthetic packs ("Cozy Cabin", "Cyberpunk"). | M | M | Monetizes the disability-income reality without subscriptions; a *tip jar with tangible value*. The theme engine already swaps everything — packs are mostly assets. |

> **★ Distribution play (red-team find, not a code feature): B2B bulk anchor licensing.**
> Sell batches of customized NFC anchors + a deploy guide to corporate wellness programs,
> schools, ADHD/recovery coaches — one-time bulk invoice. Leans 100% on the hardware moat,
> kills per-user CAC, fits one-time pricing, routes through Ascendy LLC. Possibly the
> single highest-leverage move on this page; it's sales/ops, not engineering.

## On-brand backlog (good, not yet top)

| # | Idea | Impact | Effort | Notes |
|---|------|:--:|:--:|---|
| 5 | **Printable "anchor kit" PDF** — a sheet of labeled QR anchors (Desk / Bedroom / Gym / Reading) to cut out & place. | M | S | Free growth: a printable is shareable; turns one user into a household. Pure generated PDF. |
| 6 | **Automation cookbook** — doc + ready presets for the existing `SESSION_STARTED/ENDED` Tasker intents (auto-DND, dim lights, log to a sheet). | M | S | Power-user moat, docs-first, zero app risk. Surfaces a feature most users never find. |
| 7 | **Friction-tax alternatives (calm only)** — instead of retyping, optionally a slow breath timer or a brief pause-and-reflect prompt to unlock early. Opt-in, per-list. | M | M | *(red-team: dropped squats/puzzles — forced exertion reads as punishment/shame. Keep it a gentle reset, never a penalty.)* Reframes the weak moment as relief, on-brand. |
| 8 | **Pair-a-Tag co-working** — two phones tap each other's anchors → synchronized offline session over local P2P ("focusing with Sam"). | H | L | Real-world accountability network, server-free. Strong virality but P2P is heavy. |
| 9 | **Release ritual** — a short, satisfying mascot animation when a session *ends* (can't skip ~3s) to mark focus→leisure and blunt instant relapse. | M | M | Delight + behavioral wedge against mindless re-scroll. Ties into the mascot identity. |
| 10 | **End-of-session "focus receipt"** — a shareable card (minutes focused, things blocked, streak) generated on-device. | M | M | Opt-in social proof / growth; image generated locally, no account. |
| 11 | **Wear OS tile / complication** — glanceable status + tap-to-toggle from the wrist. | M | M | Platform expansion; the anchor metaphor extends naturally to a watch tap. |
| 12 | **F-Droid release** (README says "coming soon"). | M | M | Distribution to exactly the privacy/FOSS crowd that is Ascendy's core. Concrete, overdue. |
| 13 | **Allowance / "earn-back" minutes** — finish a planned session → bank a few guilt-free distraction minutes, redeemable later. | M | M | Relief framing for inevitable breaks; opt-in. Risk: can become a loophole — design carefully. |

## ⚠️ Flagged (tension with the moat — keep but scrutinize)

| # | Idea | Tension |
|---|------|---|
| 14 | GPS/geofence auto-suggest ("at the gym → suggest lock") | Location permission cuts against permission-minimal/privacy brand even if local-only. Only if coords never persist + clearly opt-in. |
| 15 | "Shield mode" 3-sec breath interstitial (one-sec clone) | Duplicates existing a11y bounce; risks diluting the hard-lock identity. Maybe an *onboarding* gateway only. |

---

## Spike queue (cheap + verifiable first)

1. **#1 Peaceful streaks** — pure date logic in `data/Stats.kt`/streak calc → add a
   `StreakMode` (RESET | PAUSE) pref + a **Robolectric unit test** proving a missed day
   pauses (not zeroes). Verifiable headless (no device). *Do first; smallest blast radius
   with a real test.* Inspect current streak math before touching; gate behind a setting.
2. **#3 blocklist export/import** — serialize a list to a file + import; unit-test the
   round-trip. No UI risk if done as a data-layer function first.
3. **#6 automation cookbook** — docs-only, ship immediately, zero risk.

## Discovered / parking lot
- Validate market claims before building (which competitor pain is loudest? ask the audience).
- Don't ship behavior changes (streaks, blocking) **blind** — they need a device pass or a
  unit test. UI-shaped ideas can ride the Roborazzi gallery gate.

_Loop: cron `0fcac066` (every 20m). Sibling loop: UI masterpiece (`docs/UI_PROGRESS.md`)._
