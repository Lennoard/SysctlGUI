package com.androidvip.sysctlgui.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.repository.ParamRepository
import com.androidvip.sysctlgui.goAway
import com.androidvip.sysctlgui.helpers.Actions
import com.androidvip.sysctlgui.ui.settings.RemovableParamAdapter
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.ui.params.browse.KernelParamBrowserActivity
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.ui.params.list.KernelParamListActivity
import com.androidvip.sysctlgui.ui.settings.SettingsActivity
import com.androidvip.sysctlgui.utils.RootUtils
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {
    private val prefs: SharedPreferences by inject()

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

        lifecycleScope.launch {
            val isRootAccessGiven = checkRootAccess()

            splashStatusText.setText(R.string.splash_status_checking_busybox)
            val isBusyBoxAvailable = checkBusyBox()

            splashStatusText.setText(R.string.splash_status_checking_migration)
            checkForDatabaseMigration()

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

    private suspend fun checkForDatabaseMigration() {
        delay(500)
        if (!prefs.getBoolean(Prefs.MIGRATION_COMPLETED, false)) {
            splashStatusText.setText(R.string.splash_status_performing_migration)

            val repository: ParamRepository = get()
            repository.performDatabaseMigration(this)
            prefs.edit { putBoolean(Prefs.MIGRATION_COMPLETED, true) }
        }
    }

    private fun navigate() {
        val navigationIntent = when (this.intent.action) {
            Actions.KernelParamBrowserActivity.name -> {
                Intent(this, KernelParamBrowserActivity::class.java)
            }

            Actions.KernelParamsListActivity.name -> {
                Intent(this, KernelParamListActivity::class.java)
            }

            Actions.SettingsActivity.name -> {
                Intent(this, SettingsActivity::class.java)
            }

            Actions.EditParam.name -> {
                Intent(this, EditKernelParamActivity::class.java).apply {
                    putExtra(
                        RemovableParamAdapter.EXTRA_PARAM,
                        intent.getSerializableExtra(RemovableParamAdapter.EXTRA_PARAM)
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
