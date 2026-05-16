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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AscendyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateOne(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Re-render whenever the session state broadcasts say so
        if (intent.action == ACTION_REFRESH ||
            intent.action == "com.ascendy.app.SESSION_STARTED" ||
            intent.action == "com.ascendy.app.SESSION_ENDED") {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, AscendyWidget::class.java))
            ids.forEach { updateOne(context, mgr, it) }
        }
    }

    private fun updateOne(context: Context, mgr: AppWidgetManager, widgetId: Int) {
        val app = context.applicationContext as AscendyApp
        val vocab = vocabFor(app.currentVariant)
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        val active = BlockState.isActive()
        val statusText = if (active) vocab.statusFocusing else vocab.statusReady
        views.setTextViewText(R.id.widget_status, statusText)
        views.setTextViewText(R.id.widget_title, vocab.appTitle)

        // tap the widget body → open MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPi = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openPi)

        // Streak — compute off the UI thread, then re-push the view
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            val streak = Stats.streakDays(app.repo.distinctSessionDates())
            val streakText = if (streak > 0) "🔥 $streak" else "—"
            views.setTextViewText(R.id.widget_streak, streakText)
            mgr.updateAppWidget(widgetId, views)
        }
        // initial render before async streak lands
        views.setTextViewText(R.id.widget_streak, "")
        mgr.updateAppWidget(widgetId, views)
    }

    companion object {
        const val ACTION_REFRESH = "com.ascendy.app.WIDGET_REFRESH"
        fun refresh(context: Context) {
            val intent = Intent(context, AscendyWidget::class.java).setAction(ACTION_REFRESH)
            context.sendBroadcast(intent)
        }
    }
}
