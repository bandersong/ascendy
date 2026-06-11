package com.ascendy.app.ui.screens

/**
 * Hand-curated release notes shown in the "what's new" dialog on first launch after an update.
 * Add a new entry whenever you push a build that has user-visible changes worth surfacing.
 * Bumping versionCode without adding an entry here is fine — the dialog only shows when the
 * highest key here is greater than the user's last-seen version code.
 */
data class ChangelogEntry(val versionCode: Int, val title: String, val notes: List<String>)

val Changelog: List<ChangelogEntry> = listOf(
    ChangelogEntry(
        versionCode = 63,
        title = "fine print ✏️",
        notes = listOf(
            "the strict-mode toggle now explains the tradeoff before you commit, not after",
            "scheduled sessions restored after a reboot now end at the right wall-clock time across DST changes",
            "focus stats can no longer credit the wrong session log after a crash",
        )
    ),
    ChangelogEntry(
        versionCode = 50,
        title = "stability + speed 🛠️",
        notes = listOf(
            "website (DNS) blocking no longer stalls when one lookup is slow — queries run in parallel",
            "today's focus time now ticks up live during an active session",
            "fixed a widget crash when two or more widgets are placed",
            "hardened the database so future updates can't silently wipe your data",
            "quick-settings tile no longer leaks a background worker",
        )
    ),
    ChangelogEntry(
        versionCode = 49,
        title = "critical bugfixes 🐛",
        notes = listOf(
            "fixed data loss: toggling strict / allow-list mode no longer wipes your blocklist",
            "fixed blocking dying silently after the app's process was restarted mid-session",
            "fixed scheduled sessions ending unrelated focus sessions",
        )
    ),
    ChangelogEntry(
        versionCode = 47,
        title = "lockdown mode 🔒",
        notes = listOf(
            "new lockdown toggle in settings — opt-in, off by default",
            "blocks uninstalling ascendy mid-session (device-admin)",
            "seals the settings pages used to switch the blocker off during a session",
            "your safety timer is still the guaranteed way out",
        )
    ),
    ChangelogEntry(
        versionCode = 40,
        title = "polish ✨",
        notes = listOf(
            "buy-me-a-coffee link in the about screen — ko-fi.com/bandersong",
            "fixed widget memory leak; fixed strict-mode restore on cold launch",
            "what's-new card (this thing) shows once per update",
        )
    ),
    ChangelogEntry(
        versionCode = 33,
        title = "quick settings + notif actions",
        notes = listOf(
            "pull-down quick-settings tile to toggle a session",
            "end / stats action buttons on the ongoing notification",
            "settings now has tags / lists / permissions entries",
        )
    ),
    ChangelogEntry(
        versionCode = 30,
        title = "ascend mode",
        notes = listOf(
            "strict mode — override disabled, only anchor or safety timer",
            "friction-tax — type a sentence verbatim to use the emergency override",
            "mandatory safety timer — every session auto-ends after your chosen max",
            "in-app updater pulls latest builds from github releases",
        )
    ),
)

/** Returns the entries the user hasn't acknowledged yet, newest first. */
fun unseenChangelog(currentVersionCode: Int, lastSeenVersionCode: Int): List<ChangelogEntry> =
    Changelog.filter { it.versionCode in (lastSeenVersionCode + 1)..currentVersionCode }
        .sortedByDescending { it.versionCode }
