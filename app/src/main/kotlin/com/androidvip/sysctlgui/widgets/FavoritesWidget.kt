package com.androidvip.sysctlgui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.widget.RemoteViews
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.repository.ParamRepository
import com.androidvip.sysctlgui.helpers.Actions
import com.androidvip.sysctlgui.ui.SplashActivity
import com.androidvip.sysctlgui.ui.params.user.RemovableParamAdapter
import com.androidvip.sysctlgui.widgets.FavoritesWidget.Companion.EDIT_PARAM_EXTRA
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

class FavoritesWidget : AppWidgetProvider(), KoinComponent {
    private val repository: ParamRepository by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId
            )
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != EDIT_PARAM_EXTRA) {
            super.onReceive(context, intent)
            return
        }

        runBlocking {
            val params = repository.getParams(ParamRepository.SOURCE_ROOM).filter {
                it.favorite
            }.toMutableList()

            if (params.isEmpty()) return@runBlocking

            val param = params[intent.getIntExtra(EXTRA_ITEM, 0)]
            Intent(context, SplashActivity::class.java).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
                action = Actions.EditParam.name
                putExtra(RemovableParamAdapter.EXTRA_PARAM, param)
                putExtra(RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM, true)
                context.startActivity(this)
            }
        }

        super.onReceive(context, intent)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }

    companion object {
        const val EDIT_PARAM_EXTRA = "com.androidvip.sysctlgui.EDIT_PARAM_EXTRA"
        const val EXTRA_ITEM = "com.androidvip.sysctlgui.EXTRA_ITEM"
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val intent = Intent(context, FavoritesWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
    }

    val views = RemoteViews(
        context.packageName,
        R.layout.favorites_widget
    ).apply {
        setRemoteAdapter(R.id.favorites_list, intent)
        setEmptyView(R.id.favorites_list, R.id.empty_view)
    }

    val editParamPendingIntent: PendingIntent = Intent(
        context,
        FavoritesWidget::class.java
    ).run {
        action = EDIT_PARAM_EXTRA
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))

        PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    views.setPendingIntentTemplate(R.id.favorites_list, editParamPendingIntent)

    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.favorites_list)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
