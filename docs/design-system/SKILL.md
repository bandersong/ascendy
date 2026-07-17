---
name: ascendy-design
description: Use this skill to generate well-branded interfaces and assets for Ascendy (open-source Android NFC/QR focus app with Kawaii, Tough and Neutral themes), either for production or throwaway prototypes/mocks/etc. Contains essential design guidelines, colors, type, fonts, assets, and UI kit components for prototyping.
user-invocable: true
---

Read the README.md file within this skill, and explore the other available files.
If creating visual artifacts (slides, mocks, throwaway prototypes, etc), copy assets out and create static HTML files for the user to view. If working on production code, you can copy assets and read the rules here to become an expert in designing with this brand.
If the user invokes this skill without any other guidance, ask them what they want to build or design, ask some questions, and act as an expert designer who outputs HTML artifacts _or_ production code, depending on the need.

Key Ascendy rules: pick a theme scope first (kawaii / tough / neutral, each with a dark variant) — never mix palettes; every user-facing string must match that theme's voice (see the voice table in guidelines/ and Vocab.kt in https://github.com/bandersong/ascendy); use the mascot/star PNG+SVG assets verbatim, never redraw them; no gradients, no blur, flat SoftCard surfaces with Mist hairlines; press states scale to 0.97, disabled is 40% opacity; strict 4pt spacing grid.
