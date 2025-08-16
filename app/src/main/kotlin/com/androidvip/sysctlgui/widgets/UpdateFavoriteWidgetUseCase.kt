package com.androidvip.sysctlgui.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

class UpdateFavoriteWidgetUseCase(private val context: Context) {
    suspend operator fun invoke() {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(FavoritesGlanceWidget::class.java)
        glanceIds.forEach { glanceId ->
            FavoritesGlanceWidget().update(context, glanceId)
        }
    }
}
