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
