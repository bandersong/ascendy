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

    private lateinit var controller: SessionController
    private val pairingMode = MutableStateFlow(false)
    private val pendingPairedTag = MutableStateFlow<String?>(null)

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ignored, UI re-checks on resume */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val app = application as AscendyApp
        controller = SessionController(this, app.repo)

        // restore in-memory state if a session is active
        lifecycleScope.launch {
            val sess = app.repo.currentSession()
            if (sess?.active == true) {
                val pkgs = app.repo.packages(sess.listId).toSet()
                com.ascendy.app.blocking.BlockState.set(true, pkgs)
            }
        }

        setContent {
            val variant by app.themePrefs.variant.collectAsState(initial = com.ascendy.app.ui.theme.ThemeVariant.Kawaii)
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
            when (val result = controller.handleTagTap(tagId)) {
                is TapResult.Locked -> toast("focusing ✨ (${result.listName})")
                is TapResult.Unlocked -> toast("welcome back 🌸")
                is TapResult.UnknownTag -> toast("unknown tag — pair it in tags ♡")
                is TapResult.WrongTag -> toast("use the original tag to unlock")
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
    onRequestNotifications: () -> Unit,
) {
    val repo = app.repo
    val themePrefs = app.themePrefs
    val nav = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVariant by themePrefs.variant.collectAsState(initial = com.ascendy.app.ui.theme.ThemeVariant.Kawaii)

    val tags by repo.observeTags().collectAsState(initial = emptyList())
    val lists by repo.observeLists().collectAsState(initial = emptyList())
    val pairing by pairingFlow.collectAsState()
    val detected by detectedTagFlow.collectAsState()

    var permissions by remember { mutableStateOf(checkPermissions(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissions = checkPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            LaunchedEffect(Unit) { permissions = checkPermissions(context) }
            HomeScreen(
                tagCount = tags.size,
                listCount = lists.size,
                permissionsReady = permissions.accessibility || permissions.usageStats,
                onPairTag = { nav.navigate("pair") },
                onOpenLists = { nav.navigate("lists") },
                onOpenPermissions = { nav.navigate("perms") },
                onOpenSettings = { nav.navigate("settings") },
                onEmergencyUnlock = {
                    scope.launch {
                        if (controller.useEmergencyUnlock()) {
                            Toast.makeText(context, "unlocked — one use spent", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "no unlocks left", Toast.LENGTH_SHORT).show()
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
                onBack = { nav.popBackStack() }
            )
        }
        composable("apps/{listId}/{listName}") { backStack ->
            val listId = backStack.arguments?.getString("listId")?.toLongOrNull() ?: return@composable
            val listName = backStack.arguments?.getString("listName") ?: "list"
            val packages by repo.observePackages(listId).collectAsState(initial = emptyList())
            AppPickerScreen(
                listName = listName,
                blockedPackages = packages.toSet(),
                onToggle = { pkg, blocked ->
                    scope.launch {
                        if (blocked) repo.addPackage(listId, pkg) else repo.removePackage(listId, pkg)
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable("perms") {
            LaunchedEffect(Unit) { permissions = checkPermissions(context) }
            PermissionsScreen(
                status = permissions,
                onBack = { nav.popBackStack() },
                onRequestNotifications = onRequestNotifications
            )
        }
        composable("settings") {
            com.ascendy.app.ui.screens.SettingsScreen(
                current = currentVariant,
                onPickTheme = { v -> scope.launch { themePrefs.set(v) } },
                onBack = { nav.popBackStack() }
            )
        }
    }
}

private fun checkPermissions(context: Context): PermissionStatus {
    return PermissionStatus(
        accessibility = isAccessibilityEnabled(context),
        usageStats = isUsageAccessGranted(context),
        overlay = Settings.canDrawOverlays(context),
        notifications = hasNotificationPermission(context),
    )
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
