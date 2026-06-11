package com.ascendy.app

import android.app.Application
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.ThemePrefs
import com.ascendy.app.ui.theme.ThemeVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AscendyApp : Application() {
    lateinit var repo: AscendyRepo
        private set
    lateinit var themePrefs: ThemePrefs
        private set

    /**
     * Mirror of the persisted theme variant, kept in sync via a long-lived coroutine.
     * Services (notification builder, blocker activity) can read this synchronously instead
     * of doing IO on the main thread to pick a vocab.
     */
    @Volatile
    var currentVariant: ThemeVariant = ThemeVariant.Neutral
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repo = AscendyRepo(this)
        themePrefs = ThemePrefs(this)
        scope.launch {
            themePrefs.variant.collect { currentVariant = it }
        }
        scope.launch {
            val controller = com.ascendy.app.blocking.SessionController(this@AscendyApp, repo, themePrefs)
            controller.restoreOnBoot()
            controller.reconcileStaleLogs()
        }
    }
}
