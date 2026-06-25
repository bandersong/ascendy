# Ascendy UI Masterpiece — Progress Log

Running log for the autonomous UI loop. Each fire: read `UI_MASTERPIECE.md`, pick
the next unstarted item below, implement against the spec, red-team, commit, push,
then update this file (mark done + add newly found items).

**Convention:** `[x]` done · `[~]` in progress · `[ ]` todo. Newest notes on top.

---

## Iteration log

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
- [ ] **Screenshot regression harness (do this first).** Paparazzi (JVM, no
      emulator) renders every screen × 3 themes × light/dark to PNG in CI — the
      visual review gate the campaign currently lacks. Verify deps build green.
- [ ] Unified empty-states (Blocklist / Stats / Schedules) — mascot + one-line CTA
- [ ] Stats: real data-viz for daily/weekly focus (currently flat numbers)
- [ ] Hero card: progress ring around mascot for daily goal
- [ ] Consistent screen-title header component (back + title + actions)
- [ ] Haptics on toggle / session start-stop
- [ ] Reduce-motion respect (no stable Compose API yet — track via host
      Accessibility hook if needed; don't fake it)

## Guardrails added this campaign

- [x] CI lint: fail PR on raw spacing `.dp` in screens/components —
      `tools/check-design-tokens.sh` + `design-lint` job in `.github/workflows/test.yml`.
      Flags only unambiguous spacing (Spacer height/width/size, spacedBy); sizes pass.
      Validated to fire on violations and not false-positive on sizes/tokens.
