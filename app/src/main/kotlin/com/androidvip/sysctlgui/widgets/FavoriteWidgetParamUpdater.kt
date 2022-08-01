package com.androidvip.sysctlgui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.androidvip.sysctlgui.data.repository.ParamsRepositoryImpl

class FavoriteWidgetParamUpdater(private val context: Context) :
    ParamsRepositoryImpl.ChangeListener {
    override fun onChange() {
        val intentUpdate = Intent(context, FavoritesWidget::class.java)
        intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idArray = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, FavoritesWidget::class.java)
        )

        if (idArray.isEmpty()) return

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        idArray.forEach {
            intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(it))
            PendingIntent.getBroadcast(context, it, intentUpdate, flags).send()
        }
    }

    fun getListener(): ParamsRepositoryImpl.ChangeListener = this
}
