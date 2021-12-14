package com.androidvip.sysctlgui.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.databinding.ActivitySplashBinding
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.PerformDatabaseMigrationUseCase
import com.androidvip.sysctlgui.goAway
import com.androidvip.sysctlgui.helpers.Actions
import com.androidvip.sysctlgui.ui.main.MainActivity
import com.androidvip.sysctlgui.ui.params.browse.KernelParamBrowserActivity
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.ui.params.list.KernelParamListActivity
import com.androidvip.sysctlgui.ui.params.user.RemovableParamAdapter
import com.androidvip.sysctlgui.ui.settings.SettingsActivity
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val prefs: AppPrefs by inject()
    private val rootUtils: RootUtils by inject()
    private val performDatabaseMigrationUseCase: PerformDatabaseMigrationUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.colorPrimaryLight
        )

        lifecycleScope.launch {
            val isRootAccessGiven = checkRootAccess()

            binding.splashStatusText.setText(R.string.splash_status_checking_busybox)
            val isBusyBoxAvailable = checkBusyBox()

            binding.splashStatusText.setText(R.string.splash_status_checking_migration)
            checkForDatabaseMigration()

            if (isRootAccessGiven) {
                if (!isBusyBoxAvailable) {
                    prefs.useBusybox = false
                }
                navigate()
                finish()
            } else {
                binding.splashProgress.goAway()
                AlertDialog.Builder(this@StartActivity)
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

    private suspend fun checkBusyBox() = rootUtils.isBusyboxAvailable().also {
        delay(500)
    }

    private suspend fun checkForDatabaseMigration() {
        delay(500)
        if (!prefs.migrationCompleted) {
            binding.splashStatusText.setText(R.string.splash_status_performing_migration)

            val result = performDatabaseMigrationUseCase.execute()
            prefs.migrationCompleted = result.isSuccess
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
                        intent.extras!!.getParcelable<KernelParam>(RemovableParamAdapter.EXTRA_PARAM)
                    )
                    putExtra(
                        RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM,
                        intent.getBooleanExtra(
                            RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM, false
                        )
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
