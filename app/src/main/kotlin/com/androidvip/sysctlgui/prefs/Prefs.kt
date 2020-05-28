package com.androidvip.sysctlgui.prefs

import android.content.Context
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.prefs.base.BasePrefs
import com.androidvip.sysctlgui.prefs.base.Prefs


class Prefs {
    companion object : Prefs {
        private const val USER_PARAMS_FILENAME = "user-params.json"
        const val LIST_FOLDERS_FIRST = "list_folders_first"
        const val GUESS_INPUT_TYPE = "guess_input_type"
        const val COMMIT_MODE = "commit_mode"
        const val ALLOW_BLANK = "allow_blank_values"
        const val USE_BUSYBOX = "use_busybox"
        const val RUN_ON_START_UP = "run_on_start_up"
        const val START_UP_DELAY = "startup_delay"

        override fun getUserParamsSet(context: Context?): MutableList<KernelParameter> {
            return BasePrefs.getUserParamsSet(context, USER_PARAMS_FILENAME)
        }

        override fun putParam(param: KernelParameter, context: Context?): Boolean {
            return BasePrefs.putParam(param, context, USER_PARAMS_FILENAME)
        }

        override fun removeParam(param: KernelParameter, context: Context?): Boolean {
            return BasePrefs.removeParam(param, context, USER_PARAMS_FILENAME)
        }

        override fun putParams(params: MutableList<KernelParameter>, context: Context?): Boolean {
            return BasePrefs.putParams(params, context, USER_PARAMS_FILENAME)
        }

        override fun removeAllParams(context: Context?): MutableList<KernelParameter> {
            return BasePrefs.removeAllParams(context, USER_PARAMS_FILENAME)
        }
    }
}
