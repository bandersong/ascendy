#!/usr/bin/env python3
"""Independent re-derivation of DESIGN_DIRECTION.md contrast claims.
Parses the doc's own palette code-blocks, recomputes WCAG 2.x ratios, and
compares against the ratios the doc TABULATES. Trusts nothing but the hexes.
"""
import re, sys, itertools

DOC = "/Users/creative/ascendy/docs/DESIGN_DIRECTION.md"

def srgb_to_lin(c):
    c = c / 255.0
    return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4

def lum(hexs):
    h = hexs.lstrip('#')
    r, g, b = (int(h[i:i+2], 16) for i in (0, 2, 4))
    return 0.2126*srgb_to_lin(r) + 0.7152*srgb_to_lin(g) + 0.0722*srgb_to_lin(b)

def ratio(a, b):
    la, lb = lum(a), lum(b)
    hi, lo = max(la, lb), min(la, lb)
    return (hi + 0.05) / (lo + 0.05)

text = open(DOC).read()

# --- 1. self-test against known-good values from the repo's own a11y audit ---
# (published WCAG reference pairs)
selftest = [("#000000", "#FFFFFF", 21.00), ("#767676", "#FFFFFF", 4.54),
            ("#FFFFFF", "#FFFFFF", 1.00), ("#595959", "#FFFFFF", 7.00)]
for a, b, want in selftest:
    got = ratio(a, b)
    assert abs(got - want) < 0.02, f"SELFTEST FAIL {a}/{b}: got {got:.2f} want {want}"
print("selftest: OK (4 reference pairs reproduce)\n")

# --- 2. parse palette blocks ---
# blocks look like:  ground #FFF7F3   surface #FFFFFF   cloud #FBE7E0 ...
blocks = []
for m in re.finditer(r'####\s+(.+?)\n+```\n(.*?)```', text, re.S):
    name, body = m.group(1).strip(), m.group(2)
    roles = dict(re.findall(r'(\w+)\s+(#[0-9A-Fa-f]{6})', body))
    if {'ground', 'surface', 'cloud'} <= set(roles):
        blocks.append((name, roles))

print(f"parsed {len(blocks)} palette blocks: {[b[0].split('—')[0].strip() for b in blocks]}\n")

BG = ['ground', 'surface', 'cloud']
TEXT_ROLES = ['ink', 'smoke', 'accent', 'signal', 'warn']   # doc bolds these as text
NONTEXT = ['edge', 'line']                                   # non-text / boundary roles

total = fails_aa = fails_aaa = 0
text_pairs = text_aaa = 0
worst = []
mismatch = []

for name, roles in blocks:
    for fg in TEXT_ROLES + NONTEXT:
        if fg not in roles:
            continue
        for bg in BG:
            r = ratio(roles[fg], roles[bg])
            total += 1
            is_text = fg in TEXT_ROLES
            if is_text:
                text_pairs += 1
                if r >= 7.0: text_aaa += 1
                if r < 4.5:
                    fails_aa += 1
                    worst.append((name, fg, bg, r, "AA-TEXT-FAIL"))
                elif r < 7.0:
                    fails_aaa += 1
                    worst.append((name, fg, bg, r, "aaa-miss"))
            else:
                if r < 3.0:
                    fails_aa += 1
                    worst.append((name, fg, bg, r, "AA-NONTEXT-FAIL(<3:1)"))

# --- 3. cross-check the doc's TABULATED numbers against recomputation ---
cur = None
for line in text.splitlines():
    h = re.match(r'####\s+(.+)', line)
    if h:
        cur = next((b for b in blocks if b[0] == h.group(1).strip()), None)
        continue
    m = re.match(r'\|\s*`(\w+)`\s*\|\s*`(#[0-9A-Fa-f]{6})`\s*\|(.+)\|', line)
    if m and cur:
        role, hexv, rest = m.group(1), m.group(2), m.group(3)
        if cur[1].get(role, '').upper() != hexv.upper():
            mismatch.append(f"{cur[0]}: table {role}={hexv} but block says {cur[1].get(role)}")
        claimed = [float(x) for x in re.findall(r'([0-9]+\.[0-9]+)', rest)]
        for bg, c in zip(BG, claimed):
            got = ratio(hexv, cur[1][bg])
            if abs(got - c) > 0.06:
                mismatch.append(f"{cur[0]}: {role}/{bg} doc says {c:.2f}, recomputed {got:.2f}")

print(f"RECOMPUTED: {total} role×surface pairs across {len(blocks)} palettes")
print(f"  text pairs: {text_pairs}   at AAA(>=7:1): {text_aaa}   AA text failures(<4.5): "
      f"{sum(1 for w in worst if w[4]=='AA-TEXT-FAIL')}")
print(f"  non-text failures(<3:1): {sum(1 for w in worst if 'NONTEXT' in w[4])}")
print(f"\nDOC-TABLE CROSS-CHECK: {len(mismatch)} mismatches")
for m in mismatch[:20]:
    print("  !", m)
print(f"\nWORST PAIRS (bottom 12 of all {total}):")
allpairs = sorted(((ratio(r[fg], r[bg]), n, fg, bg)
                   for n, r in blocks for fg in TEXT_ROLES+NONTEXT if fg in r for bg in BG))
for v, n, fg, bg in allpairs[:12]:
    tag = "TEXT" if fg in TEXT_ROLES else "non-text"
    print(f"  {v:6.2f}  {n.split('—')[0].strip():<18} {fg:>7}/{bg:<8} [{tag}]")

print("\n=== TEXT PAIRS BELOW AAA (7:1) ===")
for n, r in blocks:
    for fg in TEXT_ROLES:
        if fg not in r: continue
        for bg in BG:
            v = ratio(r[fg], r[bg])
            if v < 7.0:
                print(f"  {v:6.2f}  {n.split('—')[0].strip():<16} {fg}/{bg}  ({r[fg]} on {r[bg]})")

print("\n=== `edge` (the 3:1 non-text duty role) MARGIN ===")
for n, r in blocks:
    if 'edge' not in r: continue
    vs = [(bg, ratio(r['edge'], r[bg])) for bg in BG]
    worst_bg, worst_v = min(vs, key=lambda x: x[1])
    flag = "  <-- ZERO MARGIN" if worst_v < 3.05 else ""
    print(f"  {n.split('—')[0].strip():<16} worst {worst_v:.2f} on {worst_bg}{flag}")
