package com.ascendy.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Per-theme copy registry. Every user-facing string in the app routes through here so the
 * voice/tone matches the active theme. Add new strings by adding a field and providing it
 * in every Vocab instance below.
 */
data class Vocab(
    // top-level
    val appTitle: String,
    val statusReady: String,
    val statusFocusing: String,
    val homeHeroIdle: String,
    val homeHeroActive: String,

    // setup section
    val sectionSetup: String,
    val rowPairTagEmoji: String,
    val rowPairTagLabel: String,
    val rowFocusListEmoji: String,
    val rowFocusListLabel: String,
    val rowPermissionsEmoji: String,
    val rowPermissionsLabel: String,
    val badgeTodo: String,
    val badgeOk: String,

    // emergency block on home
    val emergencyTitle: String,
    val emergencyBody: String,
    val emergencyButton: String,
    val emergencyUsed: String,
    val emergencyNone: String,

    // tags screen
    val tagsTitle: String,
    val tagsIntro: String,
    val tagsStartPairing: String,
    val tagsWaiting: String,
    val tagsFound: String,
    val tagsNicknameLabel: String,
    val tagsCancel: String,
    val tagsSave: String,
    val tagsListHeader: String,
    val tagsEmpty: String,
    val tagsRemove: String,

    // lists screen
    val listsTitle: String,
    val listsEmpty: String,
    val listsNewDialogTitle: String,
    val listsNewNameLabel: String,
    val listsCreate: String,
    val listsCancel: String,
    val listsBadgeDefault: String,
    val listsAppCountFmt: String,           // "%d apps"

    // app picker
    val pickerSearch: String,

    // permissions screen
    val permsTitle: String,
    val permsIntro: String,
    val permsAccessibilityEmoji: String,
    val permsAccessibilityTitle: String,
    val permsAccessibilityBody: String,
    val permsUsageEmoji: String,
    val permsUsageTitle: String,
    val permsUsageBody: String,
    val permsOverlayEmoji: String,
    val permsOverlayTitle: String,
    val permsOverlayBody: String,
    val permsNotificationsEmoji: String,
    val permsNotificationsTitle: String,
    val permsNotificationsBody: String,
    val permsOpenSettings: String,
    val permsAllow: String,
    val permsBadgeOk: String,
    val permsBadgeMissing: String,
    val permsAapmHeader: String,
    val permsAapmBody: String,

    // settings screen
    val settingsTitle: String,
    val settingsCurrentLabelFmt: String,    // "theme: kawaii ♡"
    val settingsKawaiiLabel: String,
    val settingsKawaiiTagline: String,
    val settingsToughLabel: String,
    val settingsToughTagline: String,
    val settingsNeutralLabel: String,
    val settingsNeutralTagline: String,
    val settingsBadgeActive: String,
    val settingsBadgeSelect: String,
    val settingsFooter: String,

    // blocker overlay
    val blockerTitle: String,
    val blockerBody: String,

    // toasts (use %s where variable)
    val toastLockedFmt: String,             // "%s"  = list name
    val toastUnlocked: String,
    val toastUnknownTag: String,
    val toastWrongTag: String,
)

// ───── Kawaii voice — soft, hearts and sparkles, lowercase ─────
val KawaiiVocab = Vocab(
    appTitle = "ascendy ♡",
    statusReady = "ready",
    statusFocusing = "focusing",
    homeHeroIdle = "tap your tag whenever you're ready ✨",
    homeHeroActive = "you're focusing — tap your tag to come back 🌙",

    sectionSetup = "setup",
    rowPairTagEmoji = "🌸",
    rowPairTagLabel = "pair an nfc tag",
    rowFocusListEmoji = "✨",
    rowFocusListLabel = "build your focus list",
    rowPermissionsEmoji = "🔒",
    rowPermissionsLabel = "permissions",
    badgeTodo = "todo",
    badgeOk = "ok",

    emergencyTitle = "emergency unlock",
    emergencyBody = "one-time per session. for true emergencies only.",
    emergencyButton = "use unlock",
    emergencyUsed = "unlocked — one use spent",
    emergencyNone = "no unlocks left",

    tagsTitle = "tags",
    tagsIntro = "pair a blank ntag21x sticker. you can keep it on the fridge, in a drawer, or in your bag.",
    tagsStartPairing = "start pairing",
    tagsWaiting = "hold the back of your phone against the tag ✨",
    tagsFound = "tag found 🌸",
    tagsNicknameLabel = "give it a name (e.g. kitchen)",
    tagsCancel = "cancel",
    tagsSave = "save",
    tagsListHeader = "your tags",
    tagsEmpty = "no tags yet — pair one above ♡",
    tagsRemove = "remove",

    listsTitle = "focus lists",
    listsEmpty = "no lists yet — tap + to start ✨",
    listsNewDialogTitle = "new focus list",
    listsNewNameLabel = "name (e.g. deep work)",
    listsCreate = "create",
    listsCancel = "cancel",
    listsBadgeDefault = "default",
    listsAppCountFmt = "%d apps",

    pickerSearch = "search apps",

    permsTitle = "permissions",
    permsIntro = "ascendy needs these to block apps. nothing leaves your device.",
    permsAccessibilityEmoji = "♿",
    permsAccessibilityTitle = "accessibility",
    permsAccessibilityBody = "the primary blocking path. watches the foreground app and bounces you home.",
    permsUsageEmoji = "📊",
    permsUsageTitle = "usage access",
    permsUsageBody = "fallback path used if accessibility is unavailable (e.g. android 17 advanced protection).",
    permsOverlayEmoji = "🪟",
    permsOverlayTitle = "display over other apps",
    permsOverlayBody = "lets the kawaii blocker overlay show on top of blocked apps.",
    permsNotificationsEmoji = "🔔",
    permsNotificationsTitle = "notifications",
    permsNotificationsBody = "the focus session shows a persistent notification while active (android 13+).",
    permsOpenSettings = "open settings",
    permsAllow = "allow",
    permsBadgeOk = "ok",
    permsBadgeMissing = "missing",
    permsAapmHeader = "a heads-up about android 17",
    permsAapmBody = "advanced protection mode disables accessibility services for apps not categorised as accessibility tools. ascendy isn't one — so when aapm is on, only the usage-stats path runs.",

    settingsTitle = "settings",
    settingsCurrentLabelFmt = "theme: %s",
    settingsKawaiiLabel = "kawaii ♡",
    settingsKawaiiTagline = "soft pink, blush cheeks, soothing curves",
    settingsToughLabel = "tough ⛓",
    settingsToughTagline = "iron chains, hard edges, scowling mascot",
    settingsNeutralLabel = "neutral",
    settingsNeutralTagline = "corporate, clean, no decoration",
    settingsBadgeActive = "active",
    settingsBadgeSelect = "tap",
    settingsFooter = "more themes coming soon — drop ideas via the github repo.",

    blockerTitle = "shhh… you're focusing ♡",
    blockerBody = "tap your tag to come back",

    toastLockedFmt = "focusing ✨ (%s)",
    toastUnlocked = "welcome back 🌸",
    toastUnknownTag = "unknown tag — pair it in tags ♡",
    toastWrongTag = "use the original tag to unlock",
)

// ───── Tough voice — terse, capitalised, iron/anchor metaphors ─────
val ToughVocab = Vocab(
    appTitle = "ASCENDY ⛓",
    statusReady = "READY",
    statusFocusing = "LOCKED IN",
    homeHeroIdle = "TAP THE ANCHOR. GET TO WORK.",
    homeHeroActive = "LOCKED IN. TAP THE ANCHOR TO BREAK.",

    sectionSetup = "SETUP",
    rowPairTagEmoji = "⛓",
    rowPairTagLabel = "ANCHOR",
    rowFocusListEmoji = "🔥",
    rowFocusListLabel = "BLOCKLIST",
    rowPermissionsEmoji = "🛡",
    rowPermissionsLabel = "PERMISSIONS",
    badgeTodo = "TODO",
    badgeOk = "DONE",

    emergencyTitle = "BREAK GLASS",
    emergencyBody = "one use. session only. no reset. don't waste it.",
    emergencyButton = "USE OVERRIDE",
    emergencyUsed = "OVERRIDE USED.",
    emergencyNone = "no overrides left.",

    tagsTitle = "ANCHORS",
    tagsIntro = "pair a blank ntag21x sticker. stash it somewhere annoying — drawer, glove box, gym bag. distance is the discipline.",
    tagsStartPairing = "PAIR",
    tagsWaiting = "HOLD PHONE TO TAG",
    tagsFound = "TAG ACQUIRED",
    tagsNicknameLabel = "name it (e.g. desk drawer)",
    tagsCancel = "CANCEL",
    tagsSave = "SAVE",
    tagsListHeader = "YOUR ANCHORS",
    tagsEmpty = "no anchors yet. pair one above.",
    tagsRemove = "REMOVE",

    listsTitle = "BLOCKLISTS",
    listsEmpty = "no lists. tap + to build one.",
    listsNewDialogTitle = "NEW BLOCKLIST",
    listsNewNameLabel = "name (e.g. social)",
    listsCreate = "CREATE",
    listsCancel = "CANCEL",
    listsBadgeDefault = "DEFAULT",
    listsAppCountFmt = "%d apps",

    pickerSearch = "search apps",

    permsTitle = "PERMISSIONS",
    permsIntro = "ascendy needs these to enforce the lockdown. nothing leaves your device.",
    permsAccessibilityEmoji = "⛓",
    permsAccessibilityTitle = "ACCESSIBILITY",
    permsAccessibilityBody = "primary enforcement path. watches the foreground app, bounces you home when you stray.",
    permsUsageEmoji = "🔥",
    permsUsageTitle = "USAGE ACCESS",
    permsUsageBody = "fallback path when accessibility is locked out (e.g. android 17 advanced protection).",
    permsOverlayEmoji = "🛡",
    permsOverlayTitle = "DRAW OVER OTHER APPS",
    permsOverlayBody = "the lockdown overlay covers blocked apps when they try to surface.",
    permsNotificationsEmoji = "▲",
    permsNotificationsTitle = "NOTIFICATIONS",
    permsNotificationsBody = "lockdown shows a persistent notification while active (android 13+).",
    permsOpenSettings = "OPEN SETTINGS",
    permsAllow = "ALLOW",
    permsBadgeOk = "ON",
    permsBadgeMissing = "OFF",
    permsAapmHeader = "ANDROID 17 HEADS-UP",
    permsAapmBody = "advanced protection mode kills accessibility services for non-accessibility apps. ascendy isn't one — so when aapm is on, only the usage-stats path runs.",

    settingsTitle = "SETTINGS",
    settingsCurrentLabelFmt = "theme: %s",
    settingsKawaiiLabel = "kawaii ♡",
    settingsKawaiiTagline = "soft pink, blush cheeks, soothing curves",
    settingsToughLabel = "tough ⛓",
    settingsToughTagline = "iron chains, hard edges, scowling mascot",
    settingsNeutralLabel = "neutral",
    settingsNeutralTagline = "corporate, clean, no decoration",
    settingsBadgeActive = "ACTIVE",
    settingsBadgeSelect = "TAP",
    settingsFooter = "more themes coming soon. drop ideas via the github repo.",

    blockerTitle = "LOCKED IN",
    blockerBody = "TAP YOUR ANCHOR",

    toastLockedFmt = "LOCKED IN — %s",
    toastUnlocked = "UNLOCKED. BACK TO IT.",
    toastUnknownTag = "unknown tag. pair it first.",
    toastWrongTag = "wrong tag. use the original.",
)

// ───── Neutral voice — formal, sentence-case, no decoration ─────
val NeutralVocab = Vocab(
    appTitle = "Ascendy",
    statusReady = "Idle",
    statusFocusing = "Active",
    homeHeroIdle = "Tap your tag to begin a focus session.",
    homeHeroActive = "Focus session active. Tap your tag to end it.",

    sectionSetup = "Setup",
    rowPairTagEmoji = "",
    rowPairTagLabel = "Pair a tag",
    rowFocusListEmoji = "",
    rowFocusListLabel = "Block list",
    rowPermissionsEmoji = "",
    rowPermissionsLabel = "Permissions",
    badgeTodo = "Pending",
    badgeOk = "Done",

    emergencyTitle = "Emergency override",
    emergencyBody = "Single use per session. Cannot be reset until the session ends.",
    emergencyButton = "Use override",
    emergencyUsed = "Override used.",
    emergencyNone = "No overrides remaining.",

    tagsTitle = "Tags",
    tagsIntro = "Pair a blank NTAG21x sticker. Keep it somewhere inconvenient (fridge, drawer, car).",
    tagsStartPairing = "Begin pairing",
    tagsWaiting = "Hold your phone against the tag.",
    tagsFound = "Tag detected.",
    tagsNicknameLabel = "Name (e.g. Desk)",
    tagsCancel = "Cancel",
    tagsSave = "Save",
    tagsListHeader = "Your tags",
    tagsEmpty = "No tags paired.",
    tagsRemove = "Remove",

    listsTitle = "Block lists",
    listsEmpty = "No lists yet. Tap + to create one.",
    listsNewDialogTitle = "New block list",
    listsNewNameLabel = "Name (e.g. Social)",
    listsCreate = "Create",
    listsCancel = "Cancel",
    listsBadgeDefault = "Default",
    listsAppCountFmt = "%d apps",

    pickerSearch = "Search apps",

    permsTitle = "Permissions",
    permsIntro = "Ascendy uses these to block apps. No data leaves the device.",
    permsAccessibilityEmoji = "",
    permsAccessibilityTitle = "Accessibility",
    permsAccessibilityBody = "Primary blocking path. Monitors the foreground app and returns to the home screen when a blocked app opens.",
    permsUsageEmoji = "",
    permsUsageTitle = "Usage access",
    permsUsageBody = "Fallback path used when accessibility is unavailable (e.g. Android 17 Advanced Protection).",
    permsOverlayEmoji = "",
    permsOverlayTitle = "Display over other apps",
    permsOverlayBody = "Allows the blocker overlay to display on top of blocked apps.",
    permsNotificationsEmoji = "",
    permsNotificationsTitle = "Notifications",
    permsNotificationsBody = "An ongoing notification is shown while a focus session is active (Android 13+).",
    permsOpenSettings = "Open settings",
    permsAllow = "Allow",
    permsBadgeOk = "Granted",
    permsBadgeMissing = "Missing",
    permsAapmHeader = "Android 17 notice",
    permsAapmBody = "Advanced Protection Mode disables accessibility services for apps not classified as accessibility tools. Ascendy is not. When AAPM is enabled, only the usage-stats path operates.",

    settingsTitle = "Settings",
    settingsCurrentLabelFmt = "Theme: %s",
    settingsKawaiiLabel = "Kawaii",
    settingsKawaiiTagline = "Soft pink, expressive mascot, generous curves.",
    settingsToughLabel = "Tough",
    settingsToughTagline = "Iron chains, hard edges, scowling mascot.",
    settingsNeutralLabel = "Neutral",
    settingsNeutralTagline = "Corporate, clean, no decoration.",
    settingsBadgeActive = "Active",
    settingsBadgeSelect = "Select",
    settingsFooter = "More themes can be added. Suggestions welcome via the GitHub repo.",

    blockerTitle = "Focus session active",
    blockerBody = "Tap your tag to end the session.",

    toastLockedFmt = "Session started — %s",
    toastUnlocked = "Session ended.",
    toastUnknownTag = "Unknown tag. Pair it in Tags.",
    toastWrongTag = "Use the original tag to unlock.",
)

fun vocabFor(variant: ThemeVariant): Vocab = when (variant) {
    ThemeVariant.Kawaii -> KawaiiVocab
    ThemeVariant.Tough -> ToughVocab
    ThemeVariant.Neutral -> NeutralVocab
}

val LocalVocab = staticCompositionLocalOf { KawaiiVocab }

val vocab: Vocab
    @Composable @ReadOnlyComposable
    get() = LocalVocab.current
