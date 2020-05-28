package com.androidvip.sysctlgui.prefs

import android.content.Context
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.prefs.base.BasePrefs
import com.androidvip.sysctlgui.prefs.base.Prefs

class FavoritePrefs {
    companion object: Prefs {
        private const val FAVORITES_PARAMS_FILENAME = "favorites-params.json"

        override fun getUserParamsSet(context: Context?): MutableList<KernelParameter> {
            return BasePrefs.getUserParamsSet(context, FAVORITES_PARAMS_FILENAME)
        }

        override fun putParam(param: KernelParameter, context: Context?): Boolean {
            return BasePrefs.putParam(param, context, FAVORITES_PARAMS_FILENAME)
        }

        override fun removeParam(param: KernelParameter, context: Context?): Boolean {
            return BasePrefs.removeParam(param, context, FAVORITES_PARAMS_FILENAME)
        }

        override fun putParams(params: MutableList<KernelParameter>, context: Context?): Boolean {
            return BasePrefs.putParams(params, context, FAVORITES_PARAMS_FILENAME)
        }

        override fun removeAllParams(context: Context?): MutableList<KernelParameter> {
            return BasePrefs.removeAllParams(context, FAVORITES_PARAMS_FILENAME)
        }

        fun isFavorite(param: KernelParameter, context: Context?): Boolean {
            return BasePrefs.paramExists(param, getUserParamsSet(context))
        }

    }
}