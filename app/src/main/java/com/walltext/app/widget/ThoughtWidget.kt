package com.walltext.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.walltext.app.QuickInputActivity
import com.walltext.app.R
import com.walltext.app.TextStyle
import com.walltext.app.Thought
import com.walltext.app.WallTextApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 桌面小部件：显示最新一条想法，点击直接打开 QuickInputActivity。
 */
class ThoughtWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, ThoughtWidget::class.java)
            val ids = mgr.getAppWidgetIds(cn)
            ids.forEach { id -> updateWidget(context, mgr, id) }
        }

        private fun updateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_thought)
            views.setTextViewText(R.id.widget_text, context.getString(R.string.widget_loading))
            mgr.updateAppWidget(widgetId, views)

            // 异步查最新 thought
            scope.launch {
                val latest: Thought? = try {
                    val db = (context.applicationContext as WallTextApp).database
                    db.thoughtDao().getLatest()
                } catch (_: Throwable) { null }

                val display = if (latest == null) {
                    context.getString(R.string.widget_empty)
                } else {
                    "${latest.text}\n— ${TextStyle.fromKey(latest.styleKey).displayName}"
                }

                // 点击 widget body → 打开 QuickInputActivity
                val intent = Intent(context, QuickInputActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                val pending = PendingIntent.getActivity(context, widgetId, intent, flags)

                val v = RemoteViews(context.packageName, R.layout.widget_thought)
                v.setTextViewText(R.id.widget_text, display)
                v.setOnClickPendingIntent(R.id.widget_root, pending)
                mgr.updateAppWidget(widgetId, v)
            }
        }
    }
}