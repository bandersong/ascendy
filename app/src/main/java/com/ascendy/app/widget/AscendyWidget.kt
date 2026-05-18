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
        for (id in appWidgetIds) paint(context, appWidgetManager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH ||
            intent.action == "com.ascendy.app.SESSION_STARTED" ||
            intent.action == "com.ascendy.app.SESSION_ENDED") {
            val mgr = AppWidgetManager.getInstance(context)
            mgr.getAppWidgetIds(ComponentName(context, AscendyWidget::class.java))
                .forEach { paint(context, mgr, it) }
        }
    }

    /**
     * Single paint path. Uses goAsync() + GlobalScope to do the streak DB query off the main
     * thread without leaking a per-call CoroutineScope. Renders synchronously first (status,
     * title, click intent) so the widget never appears empty, then re-renders with the streak
     * when the query completes.
     */
    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    private fun paint(context: Context, mgr: AppWidgetManager, widgetId: Int) {
        val app = context.applicationContext as AscendyApp
        val vocab = vocabFor(app.currentVariant)

        // Fast sync paint — no streak yet
        val baseViews = buildViews(context, vocab, streakText = "")
        mgr.updateAppWidget(widgetId, baseViews)

        // Async update with the streak text
        val pending = goAsync()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val streak = Stats.streakDays(app.repo.distinctSessionDates())
                val streakText = if (streak > 0) "🔥 $streak" else ""
                val views = buildViews(context, vocab, streakText)
                mgr.updateAppWidget(widgetId, views)
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
