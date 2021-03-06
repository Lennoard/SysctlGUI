package com.androidvip.sysctlgui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.androidvip.sysctlgui.data.repository.ParamRepository

class FavoriteWidgetParamUpdater(private val context: Context) : ParamRepository.ChangeListener {
    override fun onChange() {
        val intentUpdate = Intent(context, FavoritesWidget::class.java)
        intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idArray = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, FavoritesWidget::class.java)
        )

        if (idArray.isEmpty()) {
            return
        }

        idArray.forEach {
            intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(it))
            PendingIntent.getBroadcast(
                context, it, intentUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT
            ).send()
        }
    }

    fun getListener(): ParamRepository.ChangeListener = this
}