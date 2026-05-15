package com.ascendy.app

import android.app.Application
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.ThemePrefs

class AscendyApp : Application() {
    lateinit var repo: AscendyRepo
        private set
    lateinit var themePrefs: ThemePrefs
        private set

    override fun onCreate() {
        super.onCreate()
        repo = AscendyRepo(this)
        themePrefs = ThemePrefs(this)
    }
}
