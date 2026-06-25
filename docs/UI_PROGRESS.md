# Ascendy UI Masterpiece — Progress Log

Running log for the autonomous UI loop. Each fire: read `UI_MASTERPIECE.md`, pick
the next unstarted item below, implement against the spec, red-team, commit, push,
then update this file (mark done + add newly found items).

**Convention:** `[x]` done · `[~]` in progress · `[ ]` todo. Newest notes on top.

---

## Iteration log

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
- [ ] `screens/SettingsScreen.kt` (326 LOC — biggest remaining)
- [ ] `screens/AppPickerScreen.kt` (310)
- [ ] `screens/PairTagScreen.kt` (293)
- [ ] `screens/SchedulesScreen.kt` (280)
- [ ] `screens/PermissionsScreen.kt` (274)
- [ ] `screens/StatsScreen.kt` (229)
- [ ] `screens/UpdateScreen.kt` (198)
- [ ] `screens/BlocklistScreen.kt` (198)
- [ ] `screens/OnboardingScreen.kt` (169)
- [ ] `screens/AboutScreen.kt` (137)
- [ ] `screens/PomodoroScreen.kt` (117)
- [ ] `screens/Changelog.kt` (101)

## Polish backlog (after token migration)

- [ ] Press-scale feedback (`Elev.pressedScale`) on tiles/chips/cards
- [ ] Unified empty-states (Blocklist / Stats / Schedules) — mascot + one-line CTA
- [ ] Stats: real data-viz for daily/weekly focus (currently flat numbers)
- [ ] Hero card: progress ring around mascot for daily goal
- [ ] Consistent screen-title header component (back + title + actions)
- [ ] Haptics on toggle / session start-stop
- [ ] Per-variant screenshot set (3×2) checked in for regression eyeballing
- [ ] Reduce-motion respect (disable mascot bob when system setting on)

## Guardrails added this campaign

- [ ] CI lint: fail PR if a screen introduces a raw spacing `.dp` literal
