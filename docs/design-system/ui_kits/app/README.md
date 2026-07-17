# Ascendy app UI kit

Interactive recreation of the Android app (three screens), grounded in `app/src/main/java/com/ascendy/app/ui/screens/{Home,Settings,Stats}Screen.kt`.

- **Home** — header, hero SoftCard (mascot 176px + status/streak badges + goal progress + hero line or live timer), setup card with dividers and todo/done badges, Stats/Quick-lock tiles, emergency card while active. Click the mascot to toggle a session (stands in for the app's long-press).
- **Settings** — theme cards (themed icon tiles + active badge; clicking re-themes the whole app, mascot and copy included), "more" rows, goal + safety-timer chip grids, lockdown switch, footer card.
- **Stats** — streak card, three stat tiles, 7-day bar chart (best day highlighted in Lilac; bar radius follows theme), recent-session cards.

`vocab.js` holds the verbatim per-theme strings (subset of Vocab.kt). Dark-mode preview toggle floats bottom-right. Timer is sped up (1 "minute" per 4s) for demo purposes.

Not recreated (exist in the app, omitted here): onboarding, pair-tag/QR flows, app picker, permissions, schedules, pomodoro, about/updates, blocker overlay, widget.
