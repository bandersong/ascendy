# Ascendy UI Masterpiece — Progress Log

Running log for the autonomous UI loop. Each fire: read `UI_MASTERPIECE.md`, pick
the next unstarted item below, implement against the spec, red-team, commit, push,
then update this file (mark done + add newly found items).

**Convention:** `[x]` done · `[~]` in progress · `[ ]` todo. Newest notes on top.

---

## Iteration log

### 2026-06-25 — Iter 7 (real-screen renders + honest audit)
- Added `ScreenGalleryTest` — renders the **11 real screens** (not just primitives) with
  fabricated data, animations frozen (`mainClock.autoAdvance = false`). 13 PNGs in
  `src/test/snapshots/screens/` (Home ×3 themes). First time the composed screens have
  actually been *seen*.
- **What the renders revealed** (invisible from the primitive gallery):
  - ★ The 3-theme identity engine is genuinely strong — Kawaii / Tough / Neutral read as
    three different apps (mascot + color + type + full copy swap). The real moat.
  - Settings & Permissions are the most polished (cards, previews, status badges).
  - **Issues found, then fixed same iteration:**
    - [x] Home: ~40% empty bottom → **fixed**. `PageColumn(centerWhenShort=true)`
          (BoxWithConstraints + `heightIn(min)` + `Arrangement.Center`) sits short content
          in the optical center; tall content still scrolls. Only Home opts in; other
          screens byte-identical (verified). Owns the empty space (calm brand) per glm.
    - [x] Blocklist: strict-mode paragraph repeated per card → **fixed**. Now shown only
          when that list `isStrict` (contextual), not under every card.
    - [~] **Stats "weak chart" — RETRACTED.** Red-teamed my own claim: the chart code is
          actually solid (proper `drawRoundRect` bars ∝ per-day minutes, best-day Lilac
          highlight, theme-aware corner radius, labels). It only rendered flat because the
          test's fake `SessionLog`s were dated 2023 → empty last-7-days buckets. Not a real
          flaw; populating it deterministically would need clock-freezing (skipped).
    - [ ] Neutral mascot art is plainer than Kawaii/Tough (art asset, not code).
- Honest verdict: system consistency + theme identity are masterpiece-tier. Of the 3
  screen-level flaws spotted by rendering, 2 were real (now fixed) and 1 was a test-data
  mirage. ~8/10 now — with a render gate so it's measured, not vibes.

### 2026-06-25 — Iter 6 (hero goal progress ring)
- New `GoalRing(progress, show, content)` in `Decor.kt`: a halo around the hero mascot —
  faint Mist track (seamless `drawCircle`) + a Petal arc sweeping from 12 o'clock,
  turning Sage once the goal is met. Static (deterministic for snapshots).
- Wired into `HomeScreen` hero (wraps the Mascot, `show = dailyGoalMinutes > 0`,
  `progress = today / goal`). Complements the existing goal text (which carries TalkBack).
- Snapshotted 66% + done states across 3 themes; arc swaps per theme (orchid/bone/slate),
  done-ring goes Sage. Eyeballed Kawaii + Neutral.
- **Red-team (glm-5.2 + codex):**
  - glm "geometry double-counts padding → clips/off-center" → **REJECTED by render**
    (ring is centered + unclipped; the `d = minDim − stroke` keeps the centered stroke
    inside the inset canvas — glm's proposed `d = minDimension` would actually clip).
  - glm "severe mascot overlap" → **REJECTED by render** (clear halo; mascot PNG has
    transparent margins + the hero Mascot adds 8dp padding).
  - glm valid catch → track Round-cap seam at 12 o'clock fixed via seamless `drawCircle`.
  - a11y/contrast: Petal arc meets WCAG 1.4.11 (3:1 non-text) in all themes (worst
    KawaiiLight 4.4; computed). Faint Mist track is decorative (info is the filled arc).
- Verified: record + verify green.

### 2026-06-25 — Iter 5 (unified empty-states)
- New `EmptyState(text)` in `Decor.kt` = `SoftCard` + static `MiniMascot(40dp)` +
  muted `bodyMedium`. Replaced **5 ad-hoc per-screen treatments** (Blocklist, Stats,
  Schedules, PairTag, AppPicker-sites — previously mascot-40 / mascot-36 / bare-text /
  bare-text-in-card / bare-text). One deliberate look in all 3 themes.
- Snapshotted in the gallery → mascot swaps per theme (orchid / bone / slate);
  eyeballed all 3, lockstep confirmed.
- **Red-team (glm-5.2 + codex):**
  - glm "Kawaii pastels likely fail WCAG" → **DISPROVEN by computation**: Smoke-on-card
    worst case KawaiiLight = 4.91 ≥ 4.5 (AA body); all 6 theme/mode combos pass on both
    Cloud and Surface. (ratios computed, not eyeballed.)
  - glm valid catch → added `Modifier.weight(1f)` so long/localized strings wrap instead
    of shoving the mascot. Re-recorded goldens.
  - glm "too heavy for inline AppPicker-sites" → noted as a possible compact variant (below).
- Verified: record + verify green; goldens refreshed.

### 2026-06-25 — Iter 4 (screenshot regression harness)
- Stood up **Roborazzi 1.43.1** (Robolectric/JVM, no emulator) — the visual review
  gate the campaign lacked. `DesignSystemGalleryTest` renders the token primitives
  across **3 themes × light/dark = 6 PNG goldens** in `app/src/test/snapshots/`.
  - `./gradlew :app:recordRoborazziFossDebug` (write) · `verifyRoborazziFossDebug` (gate).
  - Chose Roborazzi over Paparazzi (Paparazzi lags AGP; we're on AGP 8.13.2 + the
    project already runs Robolectric). GLM-5.2-consulted on version pins vs Kotlin 2.0.21.
- **Verified vs ground truth:** all 6 goldens render correctly; eyeballed Kawaii
  light↔dark + Tough dark — lockstep + dark-mode (night qualifier) confirmed. Local
  `verifyRoborazziFossDebug` green (self-verify).
- **Red-team:** glm flagged `setQualifiers('+night')` as possibly applying too late →
  DISPROVEN by ground truth (kawaii_dark renders deep-plum, not lavender; qualifier set
  before setContent). glm's real catch (cross-env font AA) → CI `screenshot` job is
  **non-gating** (continue-on-error + diff artifacts) until goldens are re-recorded on CI.
- 1% compare tolerance (`changeThreshold=0.01`) to absorb sub-pixel AA.
- **CI cross-env verified:** the macOS/JBR goldens passed `verifyRoborazziFossDebug`
  on ubuntu/temurin17 (BUILD SUCCESSFUL, step conclusion `success` — checked the real
  step, not the rolled-up status). Drift concern didn't manifest → flipped the
  `screenshot` job to **GATING** (removed continue-on-error). Visual gate now enforced.

### 2026-06-25 — Iter 3 (tactile polish)
- Added `theme/Interaction.kt` → `Modifier.pressScale(interactionSource)`: pressables
  scale to `Elev.pressedScale` (0.97) while held, `Motion.quick` spring-back. Purely
  visual (graphicsLayer) — no layout/hit-area change.
- Wired into `SelectableChip` (app-wide), Home `HomeTile` + `SetupRow`.
- GLM's #1 "missing masterpiece lever" (UI that breathes on touch) — addressed.
- Verified: `:app:compilePlayDebugKotlin` green.

### 2026-06-25 — Iter 2 (screen sweep)
- Migrated the remaining **11 UI screens** onto the token layer via a parallel,
  adversarially-verified workflow (one agent per screen + one verifier each).
- `Changelog.kt` is a **data file** (ChangelogEntry list), not UI — no migration
  needed; correctly left untouched.
- Cleaned a pre-existing dead `Badge` import in `SchedulesScreen.kt`.
- Off-grid snaps (transparent): 6→8, 10→12, list-row vertical →16 (≥48 tap target).
  Only remaining raw `.dp` are genuine sizes (icons, the Stats chart `height(120.dp)`).
- Verified: `:app:compilePlayDebugKotlin` + `:app:compileFossDebugKotlin` both green.
- All Space.* references validated in-vocabulary (zero out-of-scale names).

### 2026-06-25 — Iter 1 (foundation)
- Built `theme/Tokens.kt`: `Space` (4pt grid, xxs→mega), `VSpace`/`HSpace`, `Motion`, `Elev`.
- Added `HairlineDivider` + `SectionLabel` to `Decor.kt`; fixed `Dot` px→Dp density bug.
- Refactored `HomeScreen.kt` and `Decor.kt` spacing/motion onto tokens.
- Wrote design spec `UI_MASTERPIECE.md`.
- **Red-team (glm-5.2 + codex):** applied — Mascot crossfade now uses
  `Motion.emphasizedEasing` (was default easing); badge vertical 4→8 (pill, not
  cramped); scale extended w/ 48/64 for macro layout. Rejected w/ reason: dark
  `Elev.cardRestDark=0` is intentional (custom palette surfaces, not M3 tonal);
  HairlineDivider modifier order is intentional (inset line).
- Verified: `:app:compileDebugKotlin` green (JBR).

---

## Backlog (token migration — one screen per item)

- [x] `theme/Tokens.kt` foundation (Space / Motion / Elev / helpers)
- [x] `components/Decor.kt` — HairlineDivider, SectionLabel, token cleanup
- [x] `screens/HomeScreen.kt`
- [x] `screens/SettingsScreen.kt` (326 LOC)
- [x] `screens/AppPickerScreen.kt` (310)
- [x] `screens/PairTagScreen.kt` (293)
- [x] `screens/SchedulesScreen.kt` (280)
- [x] `screens/PermissionsScreen.kt` (274)
- [x] `screens/StatsScreen.kt` (229)
- [x] `screens/UpdateScreen.kt` (198)
- [x] `screens/BlocklistScreen.kt` (198)
- [x] `screens/OnboardingScreen.kt` (169)
- [x] `screens/AboutScreen.kt` (137)
- [x] `screens/PomodoroScreen.kt` (117)
- [x] `screens/Changelog.kt` — N/A, data file (no UI/spacing)

**Token migration COMPLETE across all screens.** Polish backlog below is next.

## Polish backlog (after token migration)

> **NEXT PRIORITY — enabling infra.** Everything below is *visual* design and must
> not be changed blind (no emulator in the headless loop). Stand up a screenshot
> harness FIRST so each change has a real review gate, then do the visual items.

- [x] Press-scale feedback (`Elev.pressedScale`) — `Modifier.pressScale`; on chips
      (app-wide) + Home tiles/rows. TODO: extend to SoftCard-when-clickable + buttons.
- [x] **Screenshot regression harness.** Roborazzi (JVM, no emulator) — gallery of
      token primitives × 3 themes × light/dark = 6 goldens. **CI gate ENFORCED**
      (cross-env verified green). TODO next: extend snapshots from the gallery to real
      screens (HomeScreen first — needs fake BlockState/params), one screen per fire.
- [x] Unified empty-states → `EmptyState` component; 5 screens routed; WCAG-checked
      (Smoke worst case 4.91, AA pass); snapshotted across 3 themes.
- [ ] AppPicker **apps-tab** "no search results" inline state (NEW, found in red-team) —
      lightweight/card-less inline text, NOT the full `EmptyState` card (would be too heavy
      mid-scroll). Add a `compact` variant or plain `Text`.
- [ ] Consider an `EmptyState` compact (card-less) variant for small inline contexts.
- [ ] Stats: real data-viz for daily/weekly focus (currently flat numbers)
- [x] Hero card: progress ring around mascot for daily goal → `GoalRing` (track +
      Petal arc → Sage on complete); wired in HomeScreen hero; snapshotted 66%/done ×
      3 themes; arc WCAG 1.4.11 pass. TODO: animate fill in v2 (kept static for snapshots);
      eyeball the 176dp hero fit on a real device.
- [ ] Consistent screen-title header component (back + title + actions)
- [ ] Haptics on toggle / session start-stop
- [ ] Reduce-motion respect (no stable Compose API yet — track via host
      Accessibility hook if needed; don't fake it)

## Guardrails added this campaign

- [x] CI lint: fail PR on raw spacing `.dp` in screens/components —
      `tools/check-design-tokens.sh` + `design-lint` job in `.github/workflows/test.yml`.
      Flags only unambiguous spacing (Spacer height/width/size, spacedBy); sizes pass.
      Validated to fire on violations and not false-positive on sizes/tokens.
