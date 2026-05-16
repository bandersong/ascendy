package com.ascendy.app

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ascendy.app.blocking.SessionController
import com.ascendy.app.blocking.TapResult
import com.ascendy.app.data.Blocklist
import com.ascendy.app.nfc.NfcManager
import com.ascendy.app.service.BlockingAccessibilityService
import com.ascendy.app.ui.screens.AppPickerScreen
import com.ascendy.app.ui.screens.BlocklistScreen
import com.ascendy.app.ui.screens.HomeScreen
import com.ascendy.app.ui.screens.PairTagScreen
import com.ascendy.app.ui.screens.PermissionStatus
import com.ascendy.app.ui.screens.PermissionsScreen
import com.ascendy.app.ui.theme.AscendyTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ROUTE = "ascendy_route"
    }

    private lateinit var controller: SessionController
    private val pairingMode = MutableStateFlow(false)
    private val pendingPairedTag = MutableStateFlow<String?>(null)
    private val pendingRoute = MutableStateFlow<String?>(null)

    @Volatile
    private var currentVocab: com.ascendy.app.ui.theme.Vocab = com.ascendy.app.ui.theme.NeutralVocab

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ignored, UI re-checks on resume */ }

    private val qrScanLauncher = registerForActivityResult(
        com.journeyapps.barcodescanner.ScanContract()
    ) { result ->
        val raw = result?.contents ?: return@registerForActivityResult
        val anchorId = com.ascendy.app.qr.QrTools.parseScannedPayload(raw)
        if (anchorId == null) {
            toast(currentVocab.toastScanInvalid)
            return@registerForActivityResult
        }
        lifecycleScope.launch {
            val v = currentVocab
            when (val r = controller.handleTagTap(anchorId)) {
                is TapResult.Locked -> toast(v.toastLockedFmt.format(r.listName))
                is TapResult.Unlocked -> toast(v.toastUnlocked)
                is TapResult.UnknownTag -> toast(v.toastUnknownTag)
                is TapResult.WrongTag -> toast(v.toastWrongTag)
            }
        }
    }

    private val vpnConsentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = android.content.Intent(this, com.ascendy.app.vpn.AscendyVpnService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        }
    }

    fun requestVpnConsent() {
        val prepare = android.net.VpnService.prepare(this)
        if (prepare != null) {
            vpnConsentLauncher.launch(prepare)
        } else {
            val intent = android.content.Intent(this, com.ascendy.app.vpn.AscendyVpnService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        }
    }

    fun launchQrScan() {
        val options = com.journeyapps.barcodescanner.ScanOptions().apply {
            setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
            setBeepEnabled(false)
            setOrientationLocked(true)
            setCaptureActivity(com.ascendy.app.qr.PortraitCaptureActivity::class.java)
            setPrompt("")
        }
        qrScanLauncher.launch(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val app = application as AscendyApp
        controller = SessionController(this, app.repo, app.themePrefs)

        // restore in-memory state if a session is active
        lifecycleScope.launch {
            val sess = app.repo.currentSession()
            if (sess?.active == true) {
                val list = app.repo.list(sess.listId)
                val pkgs = app.repo.packages(sess.listId).toSet()
                val doms = app.repo.domains(sess.listId).toSet()
                com.ascendy.app.blocking.BlockState.set(
                    active = true,
                    blocked = pkgs,
                    blockedDomains = doms,
                    startedAt = sess.startedAt,
                    inverted = list?.isAllowList == true,
                )
            }
        }

        // keep currentVocab in sync with the persisted theme so toasts use the right voice
        lifecycleScope.launch {
            app.themePrefs.variant.collect { v ->
                currentVocab = com.ascendy.app.ui.theme.vocabFor(v)
            }
        }

        setContent {
            val variant by app.themePrefs.variant.collectAsState(initial = com.ascendy.app.ui.theme.ThemeVariant.Neutral)
            AscendyTheme(variant = variant) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(
                        app = app,
                        controller = controller,
                        pairingFlow = pairingMode,
                        detectedTagFlow = pendingPairedTag,
                        pendingRouteFlow = pendingRoute,
                        onRequestNotifications = { requestNotificationPermission() }
                    )
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        NfcManager.enableForegroundDispatch(this)
    }

    override fun onPause() {
        super.onPause()
        NfcManager.disableForegroundDispatch(this)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        intent.getStringExtra(EXTRA_ROUTE)?.let { pendingRoute.value = it }
        val action = intent.action ?: return
        val isNfc = action == android.nfc.NfcAdapter.ACTION_NDEF_DISCOVERED ||
            action == android.nfc.NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED
        if (!isNfc) return

        if (pairingMode.value) {
            val tagId = NfcManager.pairTag(intent) ?: return
            pendingPairedTag.value = tagId
            return
        }

        val tagId = NfcManager.readTagId(intent) ?: return
        lifecycleScope.launch {
            val v = currentVocab
            when (val result = controller.handleTagTap(tagId)) {
                is TapResult.Locked -> toast(v.toastLockedFmt.format(result.listName))
                is TapResult.Unlocked -> toast(v.toastUnlocked)
                is TapResult.UnknownTag -> toast(v.toastUnknownTag)
                is TapResult.WrongTag -> toast(v.toastWrongTag)
            }
        }
    }

    private fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppNav(
    app: AscendyApp,
    controller: SessionController,
    pairingFlow: MutableStateFlow<Boolean>,
    detectedTagFlow: MutableStateFlow<String?>,
    pendingRouteFlow: MutableStateFlow<String?>,
    onRequestNotifications: () -> Unit,
) {
    val repo = app.repo
    val themePrefs = app.themePrefs
    val nav = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVariant by themePrefs.variant.collectAsState(initial = com.ascendy.app.ui.theme.ThemeVariant.Neutral)

    val tags by repo.observeTags().collectAsState(initial = emptyList())
    val lists by repo.observeLists().collectAsState(initial = emptyList())
    val pairing by pairingFlow.collectAsState()
    val detected by detectedTagFlow.collectAsState()
    val onboarded by themePrefs.onboarded.collectAsState(initial = false)
    val themesIntroSeen by themePrefs.themesIntroSeen.collectAsState(initial = true)

    var permissions by remember { mutableStateOf(checkPermissions(context)) }

    // Streak: recompute on resume from session_log distinct dates
    var streakDays by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        streakDays = com.ascendy.app.data.Stats.streakDays(repo.distinctSessionDates())
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissions = checkPermissions(context)
                scope.launch {
                    streakDays = com.ascendy.app.data.Stats.streakDays(repo.distinctSessionDates())
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Honor a pending deep-link route (notification action / tile)
    val pendingRoute by pendingRouteFlow.collectAsState()
    LaunchedEffect(pendingRoute) {
        val r = pendingRoute ?: return@LaunchedEffect
        if (onboarded) {
            nav.navigate(r)
            pendingRouteFlow.value = null
        }
    }

    val startDest = if (onboarded) "home" else "onboarding"
    NavHost(navController = nav, startDestination = startDest) {
        composable("onboarding") {
            val initialSafety by themePrefs.maxSessionMinutes.collectAsState(initial = com.ascendy.app.data.MAX_SESSION_DEFAULT_MIN)
            com.ascendy.app.ui.screens.OnboardingScreen(
                initialSafetyMinutes = initialSafety,
                onFinish = { safetyMin ->
                    scope.launch {
                        themePrefs.setMaxSessionMinutes(safetyMin)
                        themePrefs.markOnboarded()
                    }
                    nav.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            LaunchedEffect(Unit) { permissions = checkPermissions(context) }
            val emergencyUsedMsg = com.ascendy.app.ui.theme.vocab.emergencyUsed
            val emergencyNoneMsg = com.ascendy.app.ui.theme.vocab.emergencyNone
            val manualStartMsg = com.ascendy.app.ui.theme.vocab.toastManualStarted
            val manualEndMsg = com.ascendy.app.ui.theme.vocab.toastManualEnded
            val strictBlockedMsg = com.ascendy.app.ui.theme.vocab.strictManualBlockedToast
            val todayMs by repo.observeFocusMsSince(
                com.ascendy.app.data.Stats.startOfTodayMs(),
                System.currentTimeMillis()
            ).collectAsState(initial = 0L)
            val dailyGoal by themePrefs.dailyGoalMinutes.collectAsState(initial = com.ascendy.app.data.DAILY_GOAL_DEFAULT_MIN)
            HomeScreen(
                tagCount = tags.size,
                listCount = lists.size,
                permissionsReady = permissions.accessibility || permissions.usageStats,
                streakDays = streakDays,
                todayFocusedMinutes = com.ascendy.app.data.Stats.msToMinutes(todayMs),
                dailyGoalMinutes = dailyGoal,
                onPairTag = { nav.navigate("pair") },
                onOpenLists = { nav.navigate("lists") },
                onOpenPermissions = { nav.navigate("perms") },
                onOpenSettings = { nav.navigate("settings") },
                onOpenStats = { nav.navigate("stats") },
                onOpenPomodoro = { nav.navigate("pomodoro") },
                onScanQr = { (context as? MainActivity)?.launchQrScan() },
                onManualToggle = {
                    scope.launch {
                        val wasActive = com.ascendy.app.blocking.BlockState.isActive()
                        val result = controller.toggleManual()
                        val msg = when (result) {
                            com.ascendy.app.blocking.ManualEndResult.BlockedStrict -> strictBlockedMsg
                            com.ascendy.app.blocking.ManualEndResult.Ended -> manualEndMsg
                            com.ascendy.app.blocking.ManualEndResult.NoSession -> manualStartMsg
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onEmergencyUnlock = {
                    scope.launch {
                        if (controller.useEmergencyUnlock()) {
                            Toast.makeText(context, emergencyUsedMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, emergencyNoneMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
        composable("pair") {
            PairTagScreen(
                waiting = pairing,
                detectedTagId = detected,
                knownTags = tags,
                lists = lists,
                onStartPairing = { pairingFlow.value = true },
                onCancelPairing = {
                    pairingFlow.value = false
                    detectedTagFlow.value = null
                },
                onSavePairing = { nickname ->
                    val id = detected ?: return@PairTagScreen
                    scope.launch {
                        repo.saveTag(
                            com.ascendy.app.data.BoundTag(
                                tagId = id,
                                nickname = nickname,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                        pairingFlow.value = false
                        detectedTagFlow.value = null
                    }
                },
                onDeleteTag = { tag -> scope.launch { repo.deleteTag(tag) } },
                onAssignList = { tag, listId ->
                    scope.launch { repo.saveTag(tag.copy(listId = listId)) }
                },
                onSaveQrAnchor = { anchorId, nick ->
                    scope.launch {
                        repo.saveTag(
                            com.ascendy.app.data.BoundTag(
                                tagId = anchorId,
                                nickname = nick,
                                createdAt = System.currentTimeMillis(),
                                kind = "qr",
                            )
                        )
                    }
                },
                onSaveQrToGallery = { anchorId ->
                    val payload = com.ascendy.app.qr.QrTools.PAYLOAD_PREFIX + anchorId
                    val bmp = com.ascendy.app.qr.QrTools.render(payload, sizePx = 1024)
                    val uri = com.ascendy.app.qr.QrTools.saveToGallery(
                        context = context,
                        bitmap = bmp,
                        displayName = "ascendy-${anchorId.take(8)}"
                    )
                    val msg = if (uri != null)
                        com.ascendy.app.ui.theme.vocabFor(currentVariant).qrSavedToGallery
                    else
                        com.ascendy.app.ui.theme.vocabFor(currentVariant).qrSaveFailed
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                onShareQr = { anchorId ->
                    val payload = com.ascendy.app.qr.QrTools.PAYLOAD_PREFIX + anchorId
                    val bmp = com.ascendy.app.qr.QrTools.render(payload, sizePx = 1024)
                    val uri = com.ascendy.app.qr.QrTools.saveToGallery(
                        context = context,
                        bitmap = bmp,
                        displayName = "ascendy-${anchorId.take(8)}"
                    )
                    if (uri != null) {
                        val share = com.ascendy.app.qr.QrTools.buildShareIntent(uri)
                        context.startActivity(Intent.createChooser(share, null))
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable("lists") {
            val pkgCounts = remember(lists) { mutableStateOf<Map<Long, Int>>(emptyMap()) }
            LaunchedEffect(lists) {
                val map = mutableMapOf<Long, Int>()
                lists.forEach { map[it.id] = repo.packages(it.id).size }
                pkgCounts.value = map
            }
            BlocklistScreen(
                lists = lists,
                appCountFor = { id -> pkgCounts.value[id] ?: 0 },
                onOpenList = { l: Blocklist -> nav.navigate("apps/${l.id}/${l.name}") },
                onCreateList = { name ->
                    scope.launch {
                        repo.upsertList(
                            com.ascendy.app.data.Blocklist(
                                name = name,
                                isDefault = lists.isEmpty()
                            )
                        )
                    }
                },
                onDeleteList = { l -> scope.launch { repo.deleteList(l.id) } },
                onToggleStrict = { l, on ->
                    scope.launch { repo.upsertList(l.copy(isStrict = on)) }
                },
                onToggleAllowList = { l, on ->
                    scope.launch { repo.upsertList(l.copy(isAllowList = on)) }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable("apps/{listId}/{listName}") { backStack ->
            val listId = backStack.arguments?.getString("listId")?.toLongOrNull() ?: return@composable
            val listName = backStack.arguments?.getString("listName") ?: "list"
            val packages by repo.observePackages(listId).collectAsState(initial = emptyList())
            val domains by repo.observeDomains(listId).collectAsState(initial = emptyList())
            AppPickerScreen(
                listName = listName,
                blockedPackages = packages.toSet(),
                blockedDomains = domains,
                onTogglePackage = { pkg, blocked ->
                    scope.launch {
                        if (blocked) repo.addPackage(listId, pkg) else repo.removePackage(listId, pkg)
                    }
                },
                onAddDomain = { d -> scope.launch { repo.addDomain(listId, d) } },
                onRemoveDomain = { d -> scope.launch { repo.removeDomain(listId, d) } },
                onBack = { nav.popBackStack() }
            )
        }
        composable("perms") {
            LaunchedEffect(Unit) { permissions = checkPermissions(context) }
            PermissionsScreen(
                status = permissions,
                onBack = { nav.popBackStack() },
                onRequestNotifications = onRequestNotifications,
                onRequestVpn = { (context as? MainActivity)?.requestVpnConsent() }
            )
        }
        composable("settings") {
            val safetyMin by themePrefs.maxSessionMinutes.collectAsState(initial = com.ascendy.app.data.MAX_SESSION_DEFAULT_MIN)
            val goalMin by themePrefs.dailyGoalMinutes.collectAsState(initial = com.ascendy.app.data.DAILY_GOAL_DEFAULT_MIN)
            com.ascendy.app.ui.screens.SettingsScreen(
                current = currentVariant,
                safetyMinutes = safetyMin,
                dailyGoalMinutes = goalMin,
                onPickTheme = { v -> scope.launch { themePrefs.set(v) } },
                onPickSafetyMinutes = { m -> scope.launch { themePrefs.setMaxSessionMinutes(m) } },
                onPickGoalMinutes = { m -> scope.launch { themePrefs.setDailyGoalMinutes(m) } },
                onOpenStats = { nav.navigate("stats") },
                onOpenSchedules = { nav.navigate("schedules") },
                onOpenPomodoro = { nav.navigate("pomodoro") },
                onOpenUpdates = { nav.navigate("updates") },
                onBack = { nav.popBackStack() }
            )
        }
        composable("updates") {
            com.ascendy.app.ui.screens.UpdateScreen(onBack = { nav.popBackStack() })
        }
        composable("stats") {
            val todayMs by repo.observeFocusMsSince(
                com.ascendy.app.data.Stats.startOfTodayMs(),
                System.currentTimeMillis()
            ).collectAsState(initial = 0L)
            val weekMs by repo.observeFocusMsSince(
                com.ascendy.app.data.Stats.startOfWeekMs(),
                System.currentTimeMillis()
            ).collectAsState(initial = 0L)
            val allMs by repo.observeFocusMsSince(0L, System.currentTimeMillis())
                .collectAsState(initial = 0L)
            val recent by repo.observeLogsSince(
                com.ascendy.app.data.Stats.localMidnightDaysAgo(30)
            ).collectAsState(initial = emptyList())
            com.ascendy.app.ui.screens.StatsScreen(
                todayMs = todayMs,
                weekMs = weekMs,
                allTimeMs = allMs,
                streakDays = streakDays,
                recent = recent,
                onBack = { nav.popBackStack() }
            )
        }
        composable("schedules") {
            val schedules by repo.observeSchedules().collectAsState(initial = emptyList())
            com.ascendy.app.ui.screens.SchedulesScreen(
                schedules = schedules,
                lists = lists,
                onSave = { s ->
                    scope.launch {
                        val id = repo.upsertSchedule(s)
                        val saved = repo.scheduleById(id)
                        if (saved != null && saved.enabled) {
                            com.ascendy.app.service.AlarmScheduler.scheduleDailyTrigger(context, saved, isStart = true)
                        }
                    }
                },
                onDelete = { s ->
                    scope.launch {
                        com.ascendy.app.service.AlarmScheduler.cancelDailyTrigger(context, s.id, isStart = true)
                        com.ascendy.app.service.AlarmScheduler.cancelDailyTrigger(context, s.id, isStart = false)
                        repo.deleteSchedule(s.id)
                    }
                },
                onToggle = { s, en ->
                    scope.launch {
                        val updated = s.copy(enabled = en)
                        repo.upsertSchedule(updated)
                        if (en) {
                            com.ascendy.app.service.AlarmScheduler.scheduleDailyTrigger(context, updated, isStart = true)
                        } else {
                            com.ascendy.app.service.AlarmScheduler.cancelDailyTrigger(context, s.id, isStart = true)
                            com.ascendy.app.service.AlarmScheduler.cancelDailyTrigger(context, s.id, isStart = false)
                        }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable("pomodoro") {
            com.ascendy.app.ui.screens.PomodoroScreen(
                lists = lists,
                onStart = { durationMs, listId ->
                    scope.launch {
                        controller.startTimedSession(durationMs, listId)
                        nav.popBackStack()
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
    }

    // One-time themes intro dialog (after onboarding, before the user does anything else).
    if (onboarded && !themesIntroSeen) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                scope.launch { themePrefs.markThemesIntroSeen() }
            },
            title = {
                androidx.compose.material3.Text(com.ascendy.app.ui.theme.vocab.themesIntroTitle)
            },
            text = {
                androidx.compose.material3.Text(com.ascendy.app.ui.theme.vocab.themesIntroBody)
            },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    scope.launch { themePrefs.markThemesIntroSeen() }
                    nav.navigate("settings")
                }) {
                    androidx.compose.material3.Text(com.ascendy.app.ui.theme.vocab.themesIntroOpen)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    scope.launch { themePrefs.markThemesIntroSeen() }
                }) {
                    androidx.compose.material3.Text(com.ascendy.app.ui.theme.vocab.themesIntroLater)
                }
            }
        )
    }
}

private fun checkPermissions(context: Context): PermissionStatus {
    return PermissionStatus(
        accessibility = isAccessibilityEnabled(context),
        usageStats = isUsageAccessGranted(context),
        overlay = Settings.canDrawOverlays(context),
        notifications = hasNotificationPermission(context),
        batteryExempt = isBatteryOptIgnored(context),
        vpnConsented = android.net.VpnService.prepare(context) == null,
    )
}

private fun isBatteryOptIgnored(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
    val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager ?: return false
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

private fun isAccessibilityEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
    if (!am.isEnabled) return false
    val expected = context.packageName + "/" + BlockingAccessibilityService::class.java.name
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val splitter = TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabledServices)
    while (splitter.hasNext()) {
        if (splitter.next().equals(expected, ignoreCase = true)) return true
    }
    return false
}

private fun isUsageAccessGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
}

@Suppress("unused")
private fun openOverlaySettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context.packageName)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
