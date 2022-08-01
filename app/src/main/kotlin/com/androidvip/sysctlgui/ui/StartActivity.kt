package com.androidvip.sysctlgui.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.ui.main.MainActivity
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.ui.params.user.RemovableParamAdapter
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineDispatcher
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
    private val dispatcher: CoroutineDispatcher by lazy { Dispatchers.Default }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                toast(R.string.root_not_found_sum, Toast.LENGTH_LONG)
                finish()
            }
        }
    }

    private suspend fun checkRootAccess() = withContext(dispatcher) {
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

            val result = runCatching { performDatabaseMigrationUseCase() }
            prefs.migrationCompleted = result.isSuccess
        }
    }

    private fun navigate() {
        val shortcutNames = arrayOf(
            Actions.BrowseParams.name,
            Actions.ExportParams.name,
            Actions.OpenSettings.name
        )
        val nextIntent = when (intent.action) {
            in shortcutNames -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_DESTINATION, intent.action)
                }
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
                            RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM,
                            false
                        )
                    )
                }
            }

            else -> {
                Intent(this, MainActivity::class.java)
            }
        }

        startActivity(nextIntent)
    }
}
