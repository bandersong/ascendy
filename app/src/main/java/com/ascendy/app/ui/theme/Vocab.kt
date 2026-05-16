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
    val pickerTabApps: String,
    val pickerTabSites: String,
    val pickerSitesAddHint: String,
    val pickerSitesAdd: String,
    val pickerSitesEmpty: String,
    val pickerSitesNote: String,

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
    val toastManualStarted: String,
    val toastManualEnded: String,
    val toastLongPressHint: String,

    // session timer
    val timerJustStarted: String,
    val timerMinFmt: String,                // "%d min"
    val timerHourMinFmt: String,            // "%dh %dm"

    // blocked-app count badge during a session
    val homeBadgeBlockedFmt: String,        // "%d blocked"

    // emergency confirm dialog (now friction-tax)
    val emergencyConfirmTitle: String,
    val emergencyConfirmBody: String,
    val emergencyConfirmYes: String,
    val emergencyConfirmNo: String,
    val frictionPrompt: String,         // "type this exactly to unlock:"
    val frictionSentence: String,       // the verbatim sentence the user must match
    val frictionInputLabel: String,
    val frictionMatchOk: String,

    // strict mode
    val strictBadge: String,
    val strictModeNote: String,         // shown when active session is strict (no emergency UI)
    val strictToggleLabel: String,      // per-list toggle label
    val strictManualBlockedToast: String, // when long-press tries to end a strict session

    // safety timer (mandatory auto-end)
    val safetyTimerTitle: String,
    val safetyTimerBody: String,
    val safetyTimerOnboardTitle: String,
    val safetyTimerOnboardBody: String,

    // onboarding
    val onboardSkip: String,
    val onboardNext: String,
    val onboardStart: String,
    val onboardP1Title: String,
    val onboardP1Body: String,
    val onboardP2Title: String,
    val onboardP2Body: String,
    val onboardP3Title: String,
    val onboardP3Body: String,

    // settings extra entries
    val settingsRowStats: String,
    val settingsRowSchedules: String,
    val settingsRowPomodoro: String,

    // stats screen
    val statsTitle: String,
    val statsToday: String,
    val statsWeek: String,
    val statsAllTime: String,
    val statsStreakFmt: String,           // "%d-day streak"
    val statsRecent: String,
    val statsEmpty: String,
    val statsAchievement7: String,
    val statsAchievement30: String,
    val statsAchievement100: String,

    // schedules screen
    val schedulesTitle: String,
    val schedulesEmpty: String,
    val schedulesNewDialogTitle: String,
    val schedulesNickname: String,
    val schedulesList: String,
    val schedulesDays: String,
    val schedulesStartTime: String,
    val schedulesEndTime: String,
    val schedulesEnabled: String,
    val schedulesSave: String,
    val schedulesCancel: String,
    val schedulesDelete: String,
    val daysShort: List<String>,          // S M T W T F S (size 7, Sun first)

    // pomodoro screen / quick-lock
    val pomodoroTitle: String,
    val pomodoroIntro: String,
    val pomodoro15: String,
    val pomodoro25: String,
    val pomodoro50: String,
    val pomodoro90: String,
    val pomodoroStart: String,

    // per-tag list picker in PairTagScreen
    val tagListPickerLabel: String,
    val tagListPickerDefault: String,

    // themes intro dialog (one-time after onboarding)
    val themesIntroTitle: String,
    val themesIntroBody: String,
    val themesIntroOpen: String,
    val themesIntroLater: String,

    // QR
    val qrGenerateButton: String,
    val qrGeneratedTitle: String,
    val qrInstructions: String,
    val qrSaveToGallery: String,
    val qrShare: String,
    val qrSaveAnchor: String,
    val qrNicknameHint: String,
    val qrSavedToGallery: String,
    val qrSaveFailed: String,
    val homeScanLabel: String,           // a11y description
    val toastScanInvalid: String,

    // ongoing focus-session notification
    val notifTitle: String,
    val notifText: String,
    val notifActionEnd: String,
    val notifActionStats: String,

    // updater
    val settingsRowUpdate: String,
    val updateTitle: String,
    val updateCurrentFmt: String,        // "current: %s (build %d)"
    val updateChecking: String,
    val updateUpToDate: String,
    val updateAvailableFmt: String,       // "build %d · %s"
    val updateDownload: String,
    val updateDownloadingFmt: String,    // "downloading… %d%%"
    val updateReady: String,
    val updateInstall: String,
    val updateNeedsInstallPerm: String,
    val updateGrantInstallPerm: String,
    val updateError: String,
    val updateRetry: String,

    // OEM battery whitelist
    val permsBatteryEmoji: String,
    val permsBatteryTitle: String,
    val permsBatteryBody: String,
    val permsBatteryAction: String,

    // stats — chart labels
    val statsChartLabel: String,
    val statsBestDay: String,

    // about
    val settingsRowAbout: String,
    val aboutTitle: String,
    val aboutVersionFmt: String,         // "version %s · build %d"
    val aboutTagline: String,
    val aboutLinkSource: String,
    val aboutLinkPrivacy: String,
    val aboutLinkReleases: String,
    val aboutMadeWith: String,

    // whitelist mode
    val allowListToggleLabel: String,
    val allowListBadge: String,

    // daily focus goal
    val goalTitle: String,
    val goalBody: String,
    val goalProgressFmt: String,         // "%d / %d min today"
    val goalReached: String,

    // vpn
    val permsVpnEmoji: String,
    val permsVpnTitle: String,
    val permsVpnBody: String,
    val permsVpnAction: String,
    val vpnNotifTitle: String,
    val vpnNotifText: String,
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
    pickerTabApps = "apps",
    pickerTabSites = "sites",
    pickerSitesAddHint = "domain (e.g. reddit.com)",
    pickerSitesAdd = "add",
    pickerSitesEmpty = "no sites blocked yet ♡",
    pickerSitesNote = "blocks the url bar in chrome, firefox, brave, samsung, edge, ddg, opera. doh-only browsers may slip through.",

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
    toastManualStarted = "focusing without a tag ✨",
    toastManualEnded = "welcome back 🌸",
    toastLongPressHint = "hold the little guy to start without a tag ♡",

    timerJustStarted = "just getting started…",
    timerMinFmt = "%d min focused",
    timerHourMinFmt = "%dh %dm focused",

    homeBadgeBlockedFmt = "%d blocked",

    emergencyConfirmTitle = "are you sure? ♡",
    emergencyConfirmBody = "this uses your one emergency unlock for the session. it won't come back until you start a new session.",
    emergencyConfirmYes = "yes, unlock",
    emergencyConfirmNo = "nevermind",
    frictionPrompt = "type this exactly to break — every character matters:",
    frictionSentence = "i am breaking my focus session even though my little guy is going to be very sad ♡",
    frictionInputLabel = "type it here",
    frictionMatchOk = "matches ✓",

    strictBadge = "strict 🔒",
    strictModeNote = "strict mode — only the tag/qr can end this session ♡",
    strictToggleLabel = "strict mode",
    strictManualBlockedToast = "strict mode — tap your tag/qr to end ♡",

    safetyTimerTitle = "max session length",
    safetyTimerBody = "every session auto-ends after this (safety in case you lose your tag/qr).",
    safetyTimerOnboardTitle = "safety timer ✨",
    safetyTimerOnboardBody = "pick how long sessions are allowed to run before auto-ending. if you lose your tag, this is your way out.",

    onboardSkip = "skip",
    onboardNext = "next",
    onboardStart = "let's go ✨",
    onboardP1Title = "tap a tag, lock distractions ♡",
    onboardP1Body = "pair a tiny nfc sticker, then keep it somewhere a little inconvenient — the fridge, a drawer, your bag. tap it once to start focusing, tap it again to come back.",
    onboardP2Title = "build your focus list 🌸",
    onboardP2Body = "pick the apps you want gone during a session. ascendy will gently bounce you back home if you try to open one.",
    onboardP3Title = "a few permissions ♡",
    onboardP3Body = "ascendy needs accessibility, usage access, and overlay permission to do its thing. nothing leaves your phone — promise.",

    settingsRowStats = "your stats 🌸",
    settingsRowSchedules = "scheduled focus ✨",
    settingsRowPomodoro = "quick lock ⏱",

    statsTitle = "your stats ♡",
    statsToday = "today",
    statsWeek = "this week",
    statsAllTime = "all time",
    statsStreakFmt = "%d-day streak 🔥",
    statsRecent = "recent sessions",
    statsEmpty = "no sessions yet — try a focus run ✨",
    statsAchievement7 = "7-day streak: your little guy got a headband 🌸",
    statsAchievement30 = "30-day streak: a sparkle floats above them ✨",
    statsAchievement100 = "100-day streak: a crown ♡",

    schedulesTitle = "scheduled focus",
    schedulesEmpty = "no schedules yet — tap + to add one ✨",
    schedulesNewDialogTitle = "new schedule",
    schedulesNickname = "name (e.g. mornings)",
    schedulesList = "focus list",
    schedulesDays = "days",
    schedulesStartTime = "start",
    schedulesEndTime = "end",
    schedulesEnabled = "enabled",
    schedulesSave = "save",
    schedulesCancel = "cancel",
    schedulesDelete = "delete",
    daysShort = listOf("S", "M", "T", "W", "T", "F", "S"),

    pomodoroTitle = "quick lock ⏱",
    pomodoroIntro = "pick a duration. blocked apps unlock automatically when the timer ends — no tag needed.",
    pomodoro15 = "15 min",
    pomodoro25 = "25 min",
    pomodoro50 = "50 min",
    pomodoro90 = "90 min",
    pomodoroStart = "start",

    tagListPickerLabel = "this tag locks…",
    tagListPickerDefault = "default list",

    themesIntroTitle = "psst — there are themes ♡",
    themesIntroBody = "ascendy comes with three looks: neutral (where you are now), kawaii (this one ♡), and tough. swap anytime in settings — the whole app changes, mascot and all.",
    themesIntroOpen = "show me",
    themesIntroLater = "later",

    qrGenerateButton = "generate qr code ✨",
    qrGeneratedTitle = "your qr anchor ♡",
    qrInstructions = "save & print this. stick it somewhere annoying — fridge, drawer, gym bag. scan it to lock or unlock.",
    qrSaveToGallery = "save to gallery",
    qrShare = "share",
    qrSaveAnchor = "save anchor",
    qrNicknameHint = "give it a name (e.g. fridge)",
    qrSavedToGallery = "saved to gallery 🌸",
    qrSaveFailed = "couldn't save — try sharing instead ♡",
    homeScanLabel = "scan qr",
    toastScanInvalid = "that's not an ascendy qr ♡",

    notifTitle = "focusing ♡",
    notifText = "tap your tag to come back",
    notifActionEnd = "end",
    notifActionStats = "stats",

    settingsRowUpdate = "check for updates ✨",
    updateTitle = "updates",
    updateCurrentFmt = "you're on %s (build %d)",
    updateChecking = "checking github releases…",
    updateUpToDate = "you're all caught up ♡",
    updateAvailableFmt = "build %d · %s",
    updateDownload = "download",
    updateDownloadingFmt = "downloading… %d%%",
    updateReady = "ready to install ✨",
    updateInstall = "install",
    updateNeedsInstallPerm = "allow ascendy to install apks first ♡",
    updateGrantInstallPerm = "open settings",
    updateError = "couldn't check ♡",
    updateRetry = "try again",

    permsBatteryEmoji = "🔋",
    permsBatteryTitle = "battery exemption",
    permsBatteryBody = "samsung/xiaomi/oneplus love killing background apps. exempting ascendy keeps the blocker alive during long sessions.",
    permsBatteryAction = "exempt ascendy",

    statsChartLabel = "last 7 days",
    statsBestDay = "best day this week",

    settingsRowAbout = "about ♡",
    aboutTitle = "about",
    aboutVersionFmt = "version %s · build %d",
    aboutTagline = "tap an anchor. focus. tap to come back ♡",
    aboutLinkSource = "open source on github",
    aboutLinkPrivacy = "privacy policy",
    aboutLinkReleases = "all releases",
    aboutMadeWith = "made with compose ✨",

    allowListToggleLabel = "allow-only mode",
    allowListBadge = "allow-only ✨",

    goalTitle = "daily focus goal ♡",
    goalBody = "pick how many minutes you want to focus each day. your little guy cheers when you hit it.",
    goalProgressFmt = "%d / %d min today",
    goalReached = "goal reached 🌸",

    permsVpnEmoji = "🌐",
    permsVpnTitle = "vpn website blocking",
    permsVpnBody = "stronger than url-bar reading — sinkholes blocked domains at the dns layer so pages never load. an android vpn icon shows while a session is active. only your phone, no traffic leaves the device.",
    permsVpnAction = "enable vpn",
    vpnNotifTitle = "blocking websites ♡",
    vpnNotifText = "dns sinkhole active during this session",
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
    pickerTabApps = "APPS",
    pickerTabSites = "SITES",
    pickerSitesAddHint = "domain (e.g. reddit.com)",
    pickerSitesAdd = "ADD",
    pickerSitesEmpty = "no sites blocked.",
    pickerSitesNote = "kills the url bar in chrome, firefox, brave, samsung, edge, ddg, opera. doh-only browsers slip through.",

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
    toastManualStarted = "LOCKED IN. NO ANCHOR — DISCIPLINE ONLY.",
    toastManualEnded = "UNLOCKED.",
    toastLongPressHint = "HOLD THE GUY TO LOCK IN WITHOUT AN ANCHOR.",

    timerJustStarted = "JUST STARTED.",
    timerMinFmt = "%d MIN LOCKED IN",
    timerHourMinFmt = "%dH %dM LOCKED IN",

    homeBadgeBlockedFmt = "%d BLOCKED",

    emergencyConfirmTitle = "BREAK GLASS?",
    emergencyConfirmBody = "one use per session. no resets. weak move. you sure?",
    emergencyConfirmYes = "BREAK",
    emergencyConfirmNo = "STAY IN",
    frictionPrompt = "type this exactly. case-sensitive. punctuation included:",
    frictionSentence = "I AM QUITTING EARLY. THIS IS A WEAK MOVE. I OWN IT AND WILL DO BETTER NEXT TIME.",
    frictionInputLabel = "type it",
    frictionMatchOk = "MATCH",

    strictBadge = "STRICT",
    strictModeNote = "STRICT MODE — ONLY THE ANCHOR ENDS THIS. NO OVERRIDE.",
    strictToggleLabel = "STRICT MODE",
    strictManualBlockedToast = "STRICT — USE YOUR ANCHOR TO END.",

    safetyTimerTitle = "MAX SESSION LENGTH",
    safetyTimerBody = "every session auto-ends after this. fail-safe for a lost anchor.",
    safetyTimerOnboardTitle = "SAFETY TIMER",
    safetyTimerOnboardBody = "pick the max duration. if your anchor disappears, this is your exit. choose carefully — strict mode plus a long timer is a real commitment.",

    onboardSkip = "SKIP",
    onboardNext = "NEXT",
    onboardStart = "LET'S WORK",
    onboardP1Title = "ANCHOR. LOCK IN. BREAK.",
    onboardP1Body = "pair an nfc sticker. stash it somewhere annoying — drawer, glove box, gym bag. tap it once to lock in. tap again to break out.",
    onboardP2Title = "PICK YOUR NOISE",
    onboardP2Body = "name the apps that steal your time. ascendy slams the door on them the second they try to surface.",
    onboardP3Title = "PERMISSIONS — A FEW",
    onboardP3Body = "accessibility, usage access, overlay. on-device only. no telemetry. no nonsense.",

    settingsRowStats = "STATS",
    settingsRowSchedules = "SCHEDULES",
    settingsRowPomodoro = "QUICK LOCK",

    statsTitle = "STATS",
    statsToday = "TODAY",
    statsWeek = "THIS WEEK",
    statsAllTime = "ALL TIME",
    statsStreakFmt = "%d-DAY STREAK ⛓",
    statsRecent = "RECENT SESSIONS",
    statsEmpty = "no sessions yet. lock in.",
    statsAchievement7 = "7-DAY STREAK: HEADBAND EARNED.",
    statsAchievement30 = "30-DAY STREAK: SPARKLE EARNED.",
    statsAchievement100 = "100-DAY STREAK: CROWN EARNED.",

    schedulesTitle = "SCHEDULES",
    schedulesEmpty = "no schedules. tap + to add.",
    schedulesNewDialogTitle = "NEW SCHEDULE",
    schedulesNickname = "name (e.g. mornings)",
    schedulesList = "list",
    schedulesDays = "days",
    schedulesStartTime = "start",
    schedulesEndTime = "end",
    schedulesEnabled = "enabled",
    schedulesSave = "SAVE",
    schedulesCancel = "CANCEL",
    schedulesDelete = "DELETE",
    daysShort = listOf("S", "M", "T", "W", "T", "F", "S"),

    pomodoroTitle = "QUICK LOCK",
    pomodoroIntro = "pick a duration. blocks lift automatically when the timer ends. no anchor needed.",
    pomodoro15 = "15 MIN",
    pomodoro25 = "25 MIN",
    pomodoro50 = "50 MIN",
    pomodoro90 = "90 MIN",
    pomodoroStart = "LOCK IN",

    tagListPickerLabel = "THIS ANCHOR LOCKS…",
    tagListPickerDefault = "DEFAULT LIST",

    themesIntroTitle = "THEMES — PICK YOUR LOOK.",
    themesIntroBody = "three modes: neutral, kawaii, tough (this one). swap any time. settings → tough.",
    themesIntroOpen = "OPEN SETTINGS",
    themesIntroLater = "LATER",

    qrGenerateButton = "GENERATE QR ANCHOR",
    qrGeneratedTitle = "YOUR QR ANCHOR",
    qrInstructions = "save and print this. stick it somewhere inconvenient — drawer, glove box, gym bag. scan it to lock in or break out.",
    qrSaveToGallery = "SAVE TO GALLERY",
    qrShare = "SHARE",
    qrSaveAnchor = "SAVE ANCHOR",
    qrNicknameHint = "name it (e.g. desk drawer)",
    qrSavedToGallery = "SAVED.",
    qrSaveFailed = "save failed. share instead.",
    homeScanLabel = "SCAN QR",
    toastScanInvalid = "not an ascendy code.",

    notifTitle = "LOCKED IN",
    notifText = "tap your anchor to break",
    notifActionEnd = "END",
    notifActionStats = "STATS",

    settingsRowUpdate = "CHECK FOR UPDATES",
    updateTitle = "UPDATES",
    updateCurrentFmt = "ON %s (BUILD %d)",
    updateChecking = "checking github releases…",
    updateUpToDate = "UP TO DATE.",
    updateAvailableFmt = "BUILD %d · %s",
    updateDownload = "DOWNLOAD",
    updateDownloadingFmt = "DOWNLOADING… %d%%",
    updateReady = "READY TO INSTALL.",
    updateInstall = "INSTALL",
    updateNeedsInstallPerm = "ALLOW APK INSTALL FIRST.",
    updateGrantInstallPerm = "OPEN SETTINGS",
    updateError = "CHECK FAILED.",
    updateRetry = "RETRY",

    permsBatteryEmoji = "🔋",
    permsBatteryTitle = "BATTERY EXEMPTION",
    permsBatteryBody = "oem killers (samsung, oneplus, xiaomi) shut down accessibility services. exempt ascendy or the blocker dies mid-session.",
    permsBatteryAction = "EXEMPT",

    statsChartLabel = "LAST 7 DAYS",
    statsBestDay = "BEST DAY",

    settingsRowAbout = "ABOUT",
    aboutTitle = "ABOUT",
    aboutVersionFmt = "VERSION %s · BUILD %d",
    aboutTagline = "ANCHOR. LOCK IN. BREAK.",
    aboutLinkSource = "SOURCE ON GITHUB",
    aboutLinkPrivacy = "PRIVACY POLICY",
    aboutLinkReleases = "ALL BUILDS",
    aboutMadeWith = "BUILT WITH COMPOSE.",

    allowListToggleLabel = "ALLOW-ONLY MODE",
    allowListBadge = "ALLOW-ONLY",

    goalTitle = "DAILY FOCUS GOAL",
    goalBody = "set a minimum. hit it. no excuses.",
    goalProgressFmt = "%d / %d MIN TODAY",
    goalReached = "GOAL HIT.",

    permsVpnEmoji = "🌐",
    permsVpnTitle = "VPN BLOCKING",
    permsVpnBody = "dns sinkhole — slams the door at the network layer. catches what url-bar reading misses. android shows a vpn icon during sessions. on-device only. nothing leaves your phone.",
    permsVpnAction = "ENABLE",
    vpnNotifTitle = "DNS LOCKDOWN",
    vpnNotifText = "blocking domains at the dns layer",
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
    pickerTabApps = "Apps",
    pickerTabSites = "Sites",
    pickerSitesAddHint = "Domain (e.g. reddit.com)",
    pickerSitesAdd = "Add",
    pickerSitesEmpty = "No sites blocked.",
    pickerSitesNote = "Blocks URL navigation in Chrome, Firefox, Brave, Samsung Internet, Edge, DuckDuckGo, and Opera. DNS-over-HTTPS-only browsers may not be detected.",

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
    toastManualStarted = "Manual session started.",
    toastManualEnded = "Session ended.",
    toastLongPressHint = "Long-press the icon to start a session without a tag.",

    timerJustStarted = "Session just started",
    timerMinFmt = "Focused for %d min",
    timerHourMinFmt = "Focused for %dh %dm",

    homeBadgeBlockedFmt = "%d blocked",

    emergencyConfirmTitle = "Use emergency override?",
    emergencyConfirmBody = "This consumes the single override available for this session. A new override is only granted when the next session starts.",
    emergencyConfirmYes = "Use override",
    emergencyConfirmNo = "Cancel",
    frictionPrompt = "Type the sentence below exactly. Punctuation and capitalization must match:",
    frictionSentence = "I am ending this focus session before completion. I accept this decision.",
    frictionInputLabel = "Enter the sentence",
    frictionMatchOk = "Match",

    strictBadge = "Strict",
    strictModeNote = "Strict mode — only the bound tag/QR can end this session.",
    strictToggleLabel = "Strict mode",
    strictManualBlockedToast = "Strict mode is on — use your tag or QR to end the session.",

    safetyTimerTitle = "Maximum session length",
    safetyTimerBody = "Every session auto-ends after this duration. Provides a fail-safe if you lose access to your tag or QR code.",
    safetyTimerOnboardTitle = "Set a safety timer",
    safetyTimerOnboardBody = "Choose the longest a single session is allowed to run before it auto-ends. This guarantees you can recover even if you misplace your tag or QR.",

    onboardSkip = "Skip",
    onboardNext = "Next",
    onboardStart = "Get started",
    onboardP1Title = "Pair an NFC tag",
    onboardP1Body = "Pair a blank NTAG21x sticker and place it somewhere inconvenient (fridge, drawer, car). Tap it once to begin a focus session, tap it again to end it.",
    onboardP2Title = "Build a block list",
    onboardP2Body = "Choose the apps you want unavailable during a focus session. Ascendy returns to the home screen whenever a blocked app is opened.",
    onboardP3Title = "Grant permissions",
    onboardP3Body = "Ascendy uses accessibility, usage access, and the overlay permission to enforce blocking. No data leaves the device.",

    settingsRowStats = "Statistics",
    settingsRowSchedules = "Scheduled sessions",
    settingsRowPomodoro = "Timed session",

    statsTitle = "Statistics",
    statsToday = "Today",
    statsWeek = "This week",
    statsAllTime = "All time",
    statsStreakFmt = "%d-day streak",
    statsRecent = "Recent sessions",
    statsEmpty = "No sessions logged yet.",
    statsAchievement7 = "7-day streak achieved.",
    statsAchievement30 = "30-day streak achieved.",
    statsAchievement100 = "100-day streak achieved.",

    schedulesTitle = "Scheduled sessions",
    schedulesEmpty = "No schedules. Tap + to add one.",
    schedulesNewDialogTitle = "New schedule",
    schedulesNickname = "Name (e.g. Mornings)",
    schedulesList = "List",
    schedulesDays = "Days",
    schedulesStartTime = "Start",
    schedulesEndTime = "End",
    schedulesEnabled = "Enabled",
    schedulesSave = "Save",
    schedulesCancel = "Cancel",
    schedulesDelete = "Delete",
    daysShort = listOf("S", "M", "T", "W", "T", "F", "S"),

    pomodoroTitle = "Timed session",
    pomodoroIntro = "Choose a duration. The session ends automatically when the timer expires. No tag required.",
    pomodoro15 = "15 min",
    pomodoro25 = "25 min",
    pomodoro50 = "50 min",
    pomodoro90 = "90 min",
    pomodoroStart = "Start",

    tagListPickerLabel = "This tag locks…",
    tagListPickerDefault = "Default list",

    themesIntroTitle = "Pick your look",
    themesIntroBody = "Ascendy ships with three themes: Neutral (current), Kawaii, and Tough. Each restyles the whole app — colors, typography, mascot, copy. Switch in Settings whenever you want.",
    themesIntroOpen = "Open settings",
    themesIntroLater = "Later",

    qrGenerateButton = "Generate QR code",
    qrGeneratedTitle = "QR anchor",
    qrInstructions = "Save and print this QR code. Place it somewhere inconvenient (fridge, drawer, vehicle). Scan it to begin or end a focus session.",
    qrSaveToGallery = "Save to gallery",
    qrShare = "Share",
    qrSaveAnchor = "Save anchor",
    qrNicknameHint = "Name (e.g. Desk)",
    qrSavedToGallery = "Saved to gallery.",
    qrSaveFailed = "Save failed. Try sharing instead.",
    homeScanLabel = "Scan QR",
    toastScanInvalid = "Not an Ascendy QR code.",

    notifTitle = "Focus session active",
    notifText = "Tap your tag to end the session",
    notifActionEnd = "End",
    notifActionStats = "Stats",

    settingsRowUpdate = "Check for updates",
    updateTitle = "Updates",
    updateCurrentFmt = "Current version: %s (build %d)",
    updateChecking = "Checking GitHub Releases…",
    updateUpToDate = "Up to date.",
    updateAvailableFmt = "Build %d · %s",
    updateDownload = "Download",
    updateDownloadingFmt = "Downloading… %d%%",
    updateReady = "Ready to install.",
    updateInstall = "Install",
    updateNeedsInstallPerm = "Allow Ascendy to install APKs first.",
    updateGrantInstallPerm = "Open settings",
    updateError = "Could not check for updates.",
    updateRetry = "Retry",

    permsBatteryEmoji = "",
    permsBatteryTitle = "Battery optimization exemption",
    permsBatteryBody = "Aggressive OEM battery savers (Samsung, OnePlus, Xiaomi) terminate accessibility services. Exempting Ascendy keeps the blocker active throughout long sessions.",
    permsBatteryAction = "Exempt Ascendy",

    statsChartLabel = "Last 7 days",
    statsBestDay = "Best day this week",

    settingsRowAbout = "About",
    aboutTitle = "About Ascendy",
    aboutVersionFmt = "Version %s · Build %d",
    aboutTagline = "Tap a physical anchor to start a focus session. Tap again to end it.",
    aboutLinkSource = "Source code on GitHub",
    aboutLinkPrivacy = "Privacy policy",
    aboutLinkReleases = "All releases",
    aboutMadeWith = "Built with Jetpack Compose.",

    allowListToggleLabel = "Allow-only mode",
    allowListBadge = "Allow-only",

    goalTitle = "Daily focus goal",
    goalBody = "Set a daily target for focused minutes. Progress is tracked on the home screen.",
    goalProgressFmt = "%d / %d min today",
    goalReached = "Goal reached.",

    permsVpnEmoji = "",
    permsVpnTitle = "VPN-based website blocking",
    permsVpnBody = "DNS-level blocking — prevents blocked domains from resolving so pages never load. Catches DNS-over-HTTPS traffic the URL-bar reader misses. A VPN icon is shown by Android while a session is active. No traffic leaves the device.",
    permsVpnAction = "Enable VPN blocking",
    vpnNotifTitle = "VPN blocking active",
    vpnNotifText = "DNS sinkhole enforcing website blocks",
)

fun vocabFor(variant: ThemeVariant): Vocab = when (variant) {
    ThemeVariant.Kawaii -> KawaiiVocab
    ThemeVariant.Tough -> ToughVocab
    ThemeVariant.Neutral -> NeutralVocab
}

val LocalVocab = staticCompositionLocalOf { NeutralVocab }

val vocab: Vocab
    @Composable @ReadOnlyComposable
    get() = LocalVocab.current
