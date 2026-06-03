package com.ascendy.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ascendy.app.AscendyApp
import com.ascendy.app.MainActivity
import com.ascendy.app.R
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.data.Stats
import com.ascendy.app.ui.theme.vocabFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AscendyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        repaint(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH ||
            intent.action == "com.ascendy.app.SESSION_STARTED" ||
            intent.action == "com.ascendy.app.SESSION_ENDED") {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, AscendyWidget::class.java))
            repaint(context, mgr, ids)
        }
    }

    /**
     * Single repaint path for ALL widget instances. goAsync() must be called at most once per
     * onReceive: it only returns a live PendingResult on the first call within a given dispatch —
     * later calls return null, and finish() on null NPEs and crashes the process. So we call it
     * exactly once here regardless of how many widgets are placed. Every widget shows identical
     * content (status / title / streak), so one synchronous paint covers them all first (nothing
     * ever flashes empty), then one streak DB query off the main thread re-renders them, and we
     * finish() once.
     */
    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    private fun repaint(context: Context, mgr: AppWidgetManager, widgetIds: IntArray) {
        if (widgetIds.isEmpty()) return
        val app = context.applicationContext as AscendyApp
        val vocab = vocabFor(app.currentVariant)

        // Fast synchronous paint for every widget — no streak yet, never appears empty.
        val baseViews = buildViews(context, vocab, streakText = "")
        widgetIds.forEach { mgr.updateAppWidget(it, baseViews) }

        // One async pass: query the streak once, re-render all widgets, finish once.
        val pending = goAsync()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val streak = Stats.streakDays(app.repo.distinctSessionDates())
                val streakText = if (streak > 0) "🔥 $streak" else ""
                val views = buildViews(context, vocab, streakText)
                widgetIds.forEach { mgr.updateAppWidget(it, views) }
            } finally {
                pending.finish()
            }
        }
    }

    private fun buildViews(
        context: Context,
        vocab: com.ascendy.app.ui.theme.Vocab,
        streakText: String,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val active = BlockState.isActive()
        views.setTextViewText(R.id.widget_status, if (active) vocab.statusFocusing else vocab.statusReady)
        views.setTextViewText(R.id.widget_title, vocab.appTitle)
        views.setTextViewText(R.id.widget_streak, streakText)

        val openPi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openPi)
        return views
    }

    companion object {
        const val ACTION_REFRESH = "com.ascendy.app.WIDGET_REFRESH"
        fun refresh(context: Context) {
            context.sendBroadcast(Intent(context, AscendyWidget::class.java).setAction(ACTION_REFRESH))
        }
    }
}
