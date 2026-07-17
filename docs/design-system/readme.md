# ascendy design system

**Ascendy** is an open-source Android focus app (`com.ascendy.app`). Tap an NFC sticker or scan a printed QR code — a physical *anchor* — to start a focus session that blocks distracting apps and websites until you tap the anchor again. **The physical friction is the feature.** Brand voice is relief and empowerment, never fear or shame. Wordmark is lowercase: `ascendy`.

The whole app re-skins itself through **three themes** — colors, typography, shapes, mascot art, and *every user-facing string*:

- **Kawaii** — orchid/lavender + sakura pink, generous curves, hearts & sparkles, lowercase everything. Mascot: a smiling 8-point lavender star with blush cheeks.
- **Tough** — ink & bone monochrome, hard edges, Black-weight ALL-CAPS type, chain/anchor metaphors. Mascot: a scowling bone-white 5-point star.
- **Neutral** — slate iris, professional, sentence case, no emoji, no decoration. Mascot: a flat-mouthed slate 4-point diamond star.

## Sources
- GitHub: https://github.com/bandersong/ascendy (Kotlin / Jetpack Compose, Material 3). Explore it for deeper fidelity — key files: `app/src/main/java/com/ascendy/app/ui/theme/{Theme,Vocab,Tokens,Interaction}.kt`, `ui/components/Decor.kt`, `ui/screens/*.kt`, `docs/` (marketing site).
- Mounted codebase `theme/` — the four theme files above (palette hexes, spacing, motion, per-theme vocabulary).
- Uploads: mascot PNGs (3 themes × locked/unlocked) and `ascendy-star*.svg` marks — copied into `assets/`.

## CONTENT FUNDAMENTALS
Every string is per-theme (see `Vocab.kt` — a 3-way copy registry). Copy is second-person ("you"), the app is "ascendy" (never "we" as a company; it's one dev + a mascot). No fear, no shame — even Tough's sternness is self-directed discipline, and safety valves are always mentioned ("the safety timer is your way out").

**Kawaii voice** — all lowercase, soft, decorated with ♡ ✨ 🌸 🌙 at line ends. The mascot is "your little guy". Reassuring: "welcome back 🌸", "shhh… you're focusing ♡", "nothing leaves your phone — promise."
**Tough voice** — ALL CAPS for labels/status/CTAs, lowercase for explanatory body. Terse. Periods as punches: "LOCKED IN. TAP THE ANCHOR TO BREAK.", "one use. session only. no reset. don't waste it." Metaphors: anchor, iron, chains (⛓ 🔥 🛡 sparingly). Never cruel to the user's identity — "weak move" is about the action.
**Neutral voice** — sentence case, formal, zero emoji, precise: "Focus session active. Tap your tag to end it."

Shared vocabulary: *anchor* (NFC tag / QR code), *focus session*, *focus list / blocklist*, *strict mode*, *lockdown*, *safety timer*, *friction tax* (typing a verbatim sentence to break early), *emergency unlock/override* (one per session). Status pairs: ready/focusing (kawaii), READY/LOCKED IN (tough), Idle/Active (neutral). Privacy line appears everywhere: "nothing leaves your device."

## VISUAL FOUNDATIONS
- **Color:** each theme is a 10-role palette with identical role names — `Cream` (bg), `Cloud` (soft surface), `Petal` (primary accent), `Lilac`, `Mint`, `Sage` (success), `Ink` (text), `Smoke` (muted), `Mist` (dividers/hairlines), `Surface` (cards). Light + dark for each. Content on Petal is always `Cream`. Max two background colors per screen (Cream page, Surface/Cloud cards). No gradients anywhere. No imagery except mascot PNGs.
- **Type:** Android system font (Roboto). Web stand-in: Roboto from Google Fonts (flagged substitution). Kawaii: SemiBold headings, −1px display tracking. Neutral: smaller, Medium, modest. Tough: Black/ExtraBold, +0.5–1px tracking, headings and labels UPPERCASE. Body 15–16px, labels 12–13px.
- **Spacing:** strict 4pt grid (`Space` tokens 2→64). Page margin 20px, card padding 20px, card↔card 16px, section gap 20px. Content column max-width 640px, centered on wide screens.
- **Radii:** the loudest per-theme signal. Kawaii 12/16/20/28/36 (cards use 28); Tough 4/6/8/10/12 (cards 10); Neutral 6/8/10/12/16 (cards 12). Badges/pills use the theme's XL radius (fully-pill in Kawaii, squarish in Tough).
- **Cards ("SoftCard"):** Surface or Cloud fill, 1px `Mist` hairline border, 20px padding, theme-large radius. Light mode: whisper shadow (`0 1px 2px rgba(0,0,0,.08)`); dark mode: border only, no shadow.
- **Motion:** quick 150ms (taps), standard 250ms (content swaps), emphasized 400ms (hero states), mascot bob 2400ms linear loop (±8px translateY). Easings: standard `cubic-bezier(.2,0,0,1)`, emphasized `cubic-bezier(.05,.7,.1,1)`. Mascot art crossfades 350ms between locked/unlocked.
- **Press state:** every pressable scales to **0.97** while held (150ms standard easing) — no color change. Disabled = 40% opacity. No hover states on Android; on web use press-scale + slight opacity.
- **Badges:** small pills, 12px/6px padding, label type, colored fills from the palette (Sage=done, Petal=todo/strict, Mint=streak, Lilac=focusing). Text color auto-picks Ink or Cream by contrast.
- **Selectable chips:** Cloud fill + Mist border when idle; Petal fill, no border when selected; 14px padding, theme-medium radius.
- **Layout rules:** single scrolling column, no bottom nav — home is a hub with a hero card (mascot 176px, status badges, live timer) and rows/tiles below. Headers are just title + icon buttons, no app bars.
- **Transparency/blur:** none. Flat opaque surfaces only.

## ICONOGRAPHY
- **Icons:** Material Icons **Rounded** (Compose `Icons.Rounded.*`) — the app uses very few: Check, Settings, QrCodeScanner, ArrowBack. On web use Material Symbols Rounded (Google Fonts CDN, filled). This is a substitution flag: the app uses the Compose icon set.
- **Emoji as icons:** yes, deliberately — theme-dependent. Kawaii rows use 🌸 ✨ 🔒; Tough uses ⛓ 🔥 🛡 ▲; Neutral uses none (empty strings). Emoji live in the vocab, not the layout.
- **Brand marks (`assets/marks/`):** `ascendy-star.svg` (white 5-point star + two dot eyes — the launcher/notification mark; also `assets/logo.svg`), plus bone/orchid/slate recolors. Never redraw the star or mascots.
- **Mascots (`assets/mascots/`):** hand-drawn PNGs, 2 states × 3 themes. `unlocked` = idle, `locked` = focusing (kawaii closes eyes blissfully; tough scowls harder; neutral stays deadpan). Hero size 176px, bobbing ±8px. `assets/theme-icons/` holds the three themed app-icon tiles used on Settings theme cards.

## Index
- `styles.css` → `tokens/` (`colors.css`, `typography.css`, `structure.css`, `base.css`). Theming: set `data-theme="kawaii|kawaii-dark|tough|tough-dark|neutral|neutral-dark"` on any container (default = kawaii light).
- `components/core/` — ThemeScope, PageColumn, SoftCard, Badge, SelectableChip
- `components/forms/` — Button, Switch, TextField *(intentional additions: Material 3 primitives the app uses, restyled per palette)*
- `components/brand/` — Mascot
- `ui_kits/app/` — interactive recreation of the Android app (Home, Settings, Stats) with a live theme switcher
- `guidelines/` — specimen cards for the Design System tab
- `assets/` — mascots, marks, theme icons
- `SKILL.md` — agent skill entry point

**Intentional additions:** ThemeScope (web-only theme wrapper), Button/Switch/TextField (Material 3 components used throughout the app but defined by the framework, restyled here from the palette). **No custom fonts existed in the source** — system Roboto documented above.
