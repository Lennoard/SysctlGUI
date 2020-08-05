package com.androidvip.sysctlgui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.RootUtils
import com.androidvip.sysctlgui.activities.base.BaseActivity
import com.androidvip.sysctlgui.adapters.KernelParamListAdapter
import com.androidvip.sysctlgui.adapters.RemovableParamAdapter
import com.androidvip.sysctlgui.goAway
import com.androidvip.sysctlgui.helpers.Actions
import com.androidvip.sysctlgui.prefs.Prefs
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(
                this,
                R.color.colorPrimaryLight
            )
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        launch {
            val isRootAccessGiven = checkRootAccess()

            splashStatusText.setText(R.string.splash_status_checking_busybox)
            val isBusyBoxAvailable = checkBusyBox()

            if (isRootAccessGiven) {
                if (!isBusyBoxAvailable) {
                    prefs.edit { putBoolean(Prefs.USE_BUSYBOX, false) }
                }
                navigate()
                finish()
            } else {
                splashProgress.goAway()
                AlertDialog.Builder(this@SplashActivity)
                    .setTitle(R.string.error)
                    .setMessage(getString(R.string.root_not_found_sum))
                    .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private suspend fun checkRootAccess() = withContext(Dispatchers.Default) {
        delay(500)
        Shell.rootAccess()
    }

    private suspend fun checkBusyBox() = RootUtils.isBusyboxAvailable().also {
        delay(500)
    }

    private fun navigate() {
        val navigationIntent = when (this.intent.action) {
            Actions.KernelParamBrowserActivity.name -> {
                Intent(this, KernelParamBrowserActivity::class.java)
            }

            Actions.KernelParamsListActivity.name -> {
                Intent(this, KernelParamsListActivity::class.java)
            }

            Actions.SettingsActivity.name -> {
                Intent(this, SettingsActivity::class.java)
            }

            Actions.EditParam.name -> {
                Intent(this, EditKernelParamActivity::class.java).apply {
                    putExtra(
                        KernelParamListAdapter.EXTRA_PARAM,
                        intent.getSerializableExtra(KernelParamListAdapter.EXTRA_PARAM)
                    )
                    putExtra(
                        RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM,
                        intent.getBooleanExtra(RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM, false)
                    )
                }
            }

            else -> {
                Intent(this, MainActivity::class.java)
            }
        }

        startActivity(navigationIntent)
    }
}
