package com.androidvip.sysctlgui.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.prefs.FavoritePrefs
import com.androidvip.sysctlgui.widgets.FavoritesWidget.Companion.EXTRA_ITEM


class FavoritesWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return FavoritesRemoteViewsFactory(applicationContext, intent!!)
    }
}

class FavoritesRemoteViewsFactory(
    val context: Context,
    val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var widgetId: Any = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private var params: MutableList<KernelParameter> = mutableListOf()

    override fun onCreate() {
        params = FavoritePrefs(context).getUserParamsSet()
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onDataSetChanged() {
        params = FavoritePrefs(context).getUserParamsSet()
    }

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            R.layout.list_item_kernel_param_widget_list
        )

        val param = params[position]
        views.setTextViewText(R.id.listKernelParamName, param.name)
        views.setTextViewText(R.id.listKernelParamValue, param.value)

        val fillInIntent = Intent().apply {
            putExtra(EXTRA_ITEM, position)
        }

        views.setOnClickFillInIntent(R.id.listKernelParamLayout, fillInIntent)

        return views
    }

    override fun getCount(): Int = params.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        params.clear()
    }
}
