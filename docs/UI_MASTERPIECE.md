# Ascendy UI — Design System ("Masterpiece" target)

This is the **fixed target spec**. Every UI change resolves to a token here — no
freehand values. Autonomous loop fires read this first, then `UI_PROGRESS.md`.

Goal: make Ascendy feel *deliberate and calm* across all three themes
(Kawaii / Neutral / Tough), each in light + dark. Polish = consistency, rhythm,
and restraint — not more decoration.

---

## 1. Spacing — strict 4pt grid

All gaps, pads, insets resolve to `Space.*` (see `theme/Tokens.kt`). **No raw
`.dp` spacing literals in screens.** Sizes (icon size, card width caps) may still
use `.dp` — only *spacing* is tokenized.

| token        | dp | use                                            |
|--------------|----|------------------------------------------------|
| `Space.xxs`  | 2  | hairline nudge only                            |
| `Space.xs`   | 4  | label↔control, tight inline gaps               |
| `Space.sm`   | 8  | chip gaps, icon↔text, badge spacing            |
| `Space.md`   | 12 | inside-row padding, dialog inner blocks        |
| `Space.lg`   | 16 | block gap, card↔card default                   |
| `Space.xl`   | 20 | page horizontal margin, section gap, card pad  |
| `Space.xxl`  | 24 | page bottom, large section breaks              |
| `Space.xxxl` | 32 | hero breathing room                            |
| `Space.huge` | 40 | empty-state / splash vertical                  |
| `Space.xhuge`| 48 | macro layout: major section seams, FAB clear   |
| `Space.mega` | 64 | screen-level hero / empty-state centering      |

Badges/pills use `vertical = Space.sm` (8) — a deliberate pill, never 4 (cramped
on labelLarge). Off-grid values (6/10/14) are not allowed; snap to the nearest
token and fix optical balance with type, not freehand padding.

**Vertical rhythm (canonical):**
- section → section: `Space.xl` (20)
- card → card: `Space.lg` (16)
- label → its control: `Space.xs` (4)
- intra-card stacked blocks: `Space.sm` (8) or `Space.md` (12)
- page horizontal margin: `Space.xl` (20) — owned by `PageColumn`/`PageFrame`
- card internal padding: `Space.xl` (20) — owned by `SoftCard`

Use `VSpace(Space.lg)` / `HSpace(Space.sm)` instead of `Spacer(Modifier.height(..))`.

## 2. Type ramp

Owned by `Theme.kt` per-variant `Typography`. Screens **must** pull from
`MaterialTheme.typography.*` — never set `fontSize`/`fontWeight` inline.
Roles: `headlineMedium` = screen title, `titleLarge` = section label,
`titleMedium` = row/card title, `bodyMedium` = body, `bodySmall` = caption,
`labelLarge` = badges/chips. One title role per visual level — don't mix.

## 3. Color — palette tokens only

Only `palette.*` and `MaterialTheme.colorScheme.*`. Never a raw `Color(0x..)` in
a screen. Text-on-fill uses `palette.onPetal` / `onChip()` / `palette.on(bg)` so
contrast stays WCAG-AA in every variant. Status semantics: Sage = ready/success,
Lilac = focusing/active, Petal = primary action, Mint = streak/neutral-accent,
Smoke = muted/caption.

## 4. Motion — `Motion.*` tokens

| token                 | ms   | use                              |
|-----------------------|------|----------------------------------|
| `Motion.quick`        | 150  | taps, toggles, micro-feedback    |
| `Motion.standard`     | 250  | content swaps, enter/exit        |
| `Motion.emphasized`   | 400  | hero / state changes             |
| `Motion.mascotBob`    | 2400 | ambient idle loop                |

Easings: `Motion.standardEasing`, `Motion.emphasizedEasing`. No raw
`durationMillis = <int>` in screens/components.

## 5. Elevation & surfaces

`SoftCard` is the one card primitive: shape `large`, `Elev.cardRestLight` (1dp)
shadow in light, `0dp` + `palette.Mist` hairline border in dark. Don't hand-roll
`Surface` cards in screens — use `SoftCard`. Dividers use `HairlineDivider`
(palette.Mist, 1dp), never a hand-rolled Box.

## 6. Component states

- pressed: scale `Elev.pressedScale` (0.97) via `Modifier.pressScale(interactionSource)`
  (theme/Interaction.kt). Hoist a `MutableInteractionSource`, hand it to the
  clickable/Surface AND to `pressScale` so the scale tracks real press state. Wired
  into `SelectableChip` (app-wide) + Home tiles/setup rows; extend to new pressables.
- disabled: alpha `Elev.disabledAlpha` (0.4)
- selected: `SelectableChip` only (correct contrast in all 3 variants)
- touch targets ≥ 48dp; icon buttons keep default 48dp box.

## 7. Theme lockstep (hard rule)

Every visual change is validated in **all 3 variants × light + dark**. Anything
theme-specific lives in `Theme.kt`/`Vocab.kt`, never inlined in a screen. Tough =
hard edges + heavy type; Kawaii = soft radii + rounded warmth; Neutral =
restrained mid-radius. Tokens above are variant-agnostic on purpose.

## 8. Definition of done (per screen)

1. zero raw spacing `.dp` (only `Space.*` / `VSpace`/`HSpace`)
2. zero inline `fontSize`/`fontWeight`/raw `Color(...)`
3. cards via `SoftCard`, dividers via `HairlineDivider`
4. motion via `Motion.*`
5. reads correctly in 3 variants × light/dark (or noted in progress log)
