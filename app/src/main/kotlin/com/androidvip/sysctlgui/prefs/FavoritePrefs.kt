package com.androidvip.sysctlgui.prefs

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.prefs.base.BasePrefs
import com.androidvip.sysctlgui.widgets.FavoritesWidget

class FavoritePrefs(context: Context?, private var changeListener: ChangeListener? = null) :
    BasePrefs(context, fileName = "favorites-params.json"),
    BasePrefs.ChangeListener {

    init {
        changeListener = this
    }

    fun isFavorite(param: KernelParam): Boolean {
        return paramExists(param, getUserParamsSet())
    }

    override fun onChanged() {
        if (context === null) {
            return
        }

        val intentUpdate = Intent(context, FavoritesWidget::class.java)
        intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idArray = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(
                context,
                FavoritesWidget::class.java
            )
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

    override fun changeListener(): ChangeListener? = this

}
