#!/usr/bin/env python3
"""WCAG AA contrast audit of every fg/bg pair the Ascendy token system actually renders.

Palettes verbatim from app/src/main/java/com/ascendy/app/ui/theme/Theme.kt (autoloop/campaign).
Text-selection logic reproduced from:
  - Palette.on()  (Theme.kt:44-48)  -> max-contrast pick between Cream and Ink
  - onChip()      (Decor.kt:241-246) -> luminance>0.45 threshold pick
Compose Color.luminance() uses the same sRGB linearization as WCAG, so floats match.
"""

def srgb_lin(c):
    c /= 255.0
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4

def lum(hexstr):
    h = hexstr.lstrip('#')
    r, g, b = int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)
    return 0.2126 * srgb_lin(r) + 0.7152 * srgb_lin(g) + 0.0722 * srgb_lin(b)

def ratio(a, b):
    la, lb = lum(a) + 0.05, lum(b) + 0.05
    return la / lb if la > lb else lb / la

PALETTES = {
    "KawaiiLight":  dict(dark=False, Cream="FBF7FF", Cloud="F3E9FC", Petal="9D40CE", Lilac="FF9EC4", Mint="FFC59A", Sage="8FD0AC", Ink="3D2453", Smoke="7C5A82", Mist="ECDDF7", Surface="FFFDFF"),
    "KawaiiDark":   dict(dark=True,  Cream="1E1428", Cloud="2C1E3A", Petal="DDA8F5", Lilac="FFB0CE", Mint="FFC59A", Sage="9FD8B6", Ink="F7EFFB", Smoke="C4A8D4", Mist="3D2A4F", Surface="271934"),
    "ToughLight":   dict(dark=False, Cream="ECE9E1", Cloud="DAD5CB", Petal="15151A", Lilac="52504B", Mint="B0B19F", Sage="7C8A78", Ink="0C0C0E", Smoke="565249", Mist="CFC9BF", Surface="F4F1EA"),
    "ToughDark":    dict(dark=True,  Cream="070708", Cloud="161618", Petal="F2EFE4", Lilac="8B8478", Mint="7A7F71", Sage="5D695E", Ink="F2EFE4", Smoke="9C958A", Mist="28282B", Surface="121214"),
    "NeutralLight": dict(dark=False, Cream="F6F7FA", Cloud="EAECF2", Petal="4D5694", Lilac="646A7C", Mint="DEE2EE", Sage="BFE0CA", Ink="15161E", Smoke="5C6070", Mist="E1E4EB", Surface="FFFFFF"),
    "NeutralDark":  dict(dark=True,  Cream="101117", Cloud="191B23", Petal="9AA2E0", Lilac="9AA0B2", Mint="2C3344", Sage="2C3D33", Ink="E8E9F0", Smoke="9DA1B2", Mist="262932", Surface="1A1C24"),
}

def on(p, bg):          # Palette.on(): argmax contrast among Cream/Ink
    return "Cream" if ratio(p[bg], p["Cream"]) >= ratio(p[bg], p["Ink"]) else "Ink"

def onchip(p, bg):      # Decor.onChip(): luminance threshold 0.45
    dark_text = "Cream" if p["dark"] else "Ink"
    light_text = "Ink" if p["dark"] else "Cream"
    return dark_text if lum(p[bg]) > 0.45 else light_text

# (label, fg-resolver, bg, threshold, where-it-renders)
CASES = [
    ("Ink on Cream",          lambda p: "Ink",            "Cream",   4.5, "page text everywhere"),
    ("Ink on Surface",        lambda p: "Ink",            "Surface", 4.5, "card titles"),
    ("Ink on Cloud",          lambda p: "Ink",            "Cloud",   4.5, "hero card timer/title"),
    ("Ink on Mist",           lambda p: "Ink",            "Mist",    4.5, "friction dialog sentence"),
    ("Smoke on Cream",        lambda p: "Smoke",          "Cream",   4.5, "muted page text, SectionLabel"),
    ("Smoke on Surface",      lambda p: "Smoke",          "Surface", 4.5, "card subtitles 12-13sp"),
    ("Smoke on Cloud",        lambda p: "Smoke",          "Cloud",   4.5, "strict note, footer, emergency body"),
    ("Smoke on Mist",         lambda p: "Smoke",          "Mist",    4.5, "day-toggle OFF 12sp, letter avatar"),
    ("onPetal on Petal",      lambda p: on(p, "Petal"),   "Petal",   4.5, "buttons, FAB, selected chips, day-toggle ON 12sp"),
    ("Petal on Surface",      lambda p: "Petal",          "Surface", 4.5, "TextButton labels 12-13sp"),
    ("Petal on Cloud",        lambda p: "Petal",          "Cloud",   4.5, "TextButton on Cloud card (emergency)"),
    ("Badge Petal",           lambda p: onchip(p, "Petal"), "Petal", 4.5, "TO DO / MISSING / STRICT badges ~12sp"),
    ("Badge Sage",            lambda p: onchip(p, "Sage"),  "Sage",  4.5, "READY / OK / ACTIVE badges ~12sp"),
    ("Badge Lilac",           lambda p: onchip(p, "Lilac"), "Lilac", 4.5, "FOCUSING status badge ~12sp"),
    ("Badge Mint",            lambda p: onchip(p, "Mint"),  "Mint",  4.5, "streak / DEFAULT / SELECT badges ~12sp"),
    # non-text UI components: 3:1 (WCAG 1.4.11)
    ("GoalRing arc vs Cloud", lambda p: "Petal",          "Cloud",   3.0, "goal progress arc on hero card [nontext]"),
    ("Chart bar vs Surface",  lambda p: "Petal",          "Surface", 3.0, "stats week bars [nontext]"),
    ("Chart best-bar Lilac",  lambda p: "Lilac",          "Surface", 3.0, "stats highlighted bar [nontext]"),
    ("Day ON vs OFF",         lambda p: "Petal",          "Mist",    3.0, "enabled-day dot vs disabled dot [nontext state]"),
    ("Mist hairline vs Surface", lambda p: "Mist",        "Surface", 3.0, "card border / divider [nontext, informational?]"),
]

fails = []
for name, p in PALETTES.items():
    print(f"\n=== {name} ===")
    for label, fg_fn, bg, need, where in CASES:
        fg = fg_fn(p)
        r = ratio(p[fg], p[bg])
        mark = "PASS" if r >= need else "FAIL"
        if r < need:
            fails.append((name, label, fg, p[fg], bg, p[bg], r, need, where))
        print(f"  {mark}  {label:26s} {fg}(#{p[fg]}) on {bg}(#{p[bg]})  {r:5.2f}:1  (need {need})  — {where}")

print(f"\n================ {len(fails)} FAILURES ================")
for name, label, fg, fgh, bg, bgh, r, need, where in fails:
    print(f"  {name:13s} {label:26s} #{fgh} on #{bgh}  {r:4.2f}:1 < {need}  — {where}")

# divergence check: does onChip's 0.45 threshold ever pick a worse text color than Palette.on()?
print("\n=== onChip vs Palette.on divergence (accent backgrounds) ===")
for name, p in PALETTES.items():
    for bg in ("Petal", "Lilac", "Mint", "Sage"):
        a, b = onchip(p, bg), on(p, bg)
        if a != b:
            print(f"  {name}: onChip picks {a} ({ratio(p[a], p[bg]):.2f}:1) but best is {b} ({ratio(p[b], p[bg]):.2f}:1) on {bg} #{p[bg]}")
