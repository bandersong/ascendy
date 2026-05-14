package com.ascendy.app

import android.app.Application
import com.ascendy.app.data.AscendyRepo

class AscendyApp : Application() {
    lateinit var repo: AscendyRepo
        private set

    override fun onCreate() {
        super.onCreate()
        repo = AscendyRepo(this)
    }
}
