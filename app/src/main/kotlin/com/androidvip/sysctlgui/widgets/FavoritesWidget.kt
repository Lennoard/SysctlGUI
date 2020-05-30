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
import com.androidvip.sysctlgui.activities.SplashActivity
import com.androidvip.sysctlgui.adapters.KernelParamListAdapter
import com.androidvip.sysctlgui.adapters.RemovableParamAdapter
import com.androidvip.sysctlgui.helpers.Actions
import com.androidvip.sysctlgui.prefs.FavoritePrefs
import com.androidvip.sysctlgui.widgets.FavoritesWidget.Companion.EDIT_PARAM_EXTRA


class FavoritesWidget : AppWidgetProvider() {
    companion object {

        const val EDIT_PARAM_EXTRA = "com.androidvip.sysctlgui.EDIT_PARAM_EXTRA"
        const val EXTRA_ITEM = "com.androidvip.sysctlgui.EXTRA_ITEM"

    }

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
        if (intent!!.action == EDIT_PARAM_EXTRA) {
            val params = FavoritePrefs(context).getUserParamsSet()
            if (params.isEmpty()) {
                return
            }
            val param = params[intent.getIntExtra(EXTRA_ITEM, 0)]
            Intent(context, SplashActivity::class.java).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
                action = Actions.EditParam.name
                putExtra(KernelParamListAdapter.EXTRA_PARAM, param)
                putExtra(RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM, true)
                context!!.startActivity(this)
            }

        }
        super.onReceive(context, intent)
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
