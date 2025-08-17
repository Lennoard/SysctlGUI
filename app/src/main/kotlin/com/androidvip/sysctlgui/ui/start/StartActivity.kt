package com.androidvip.sysctlgui.ui.start

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.databinding.ActivitySplashBinding
import com.androidvip.sysctlgui.domain.enums.Actions
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val rootUtils: RootUtils by inject()
    private val prefs: AppPrefs by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { true }
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            rootUtils.getRootShell()
            val isRootAccessGiven = checkRootAccess()

            binding.splashStatusText.setText(R.string.splash_status_checking_busybox)
            val isBusyBoxAvailable = checkBusyBox()

            if (isRootAccessGiven) {
                if (!isBusyBoxAvailable) {
                    prefs.useBusybox = false
                }
                navigate()
                finish()
            } else {
                binding.splashProgress.isVisible = false
                toast(R.string.root_not_found_sum, Toast.LENGTH_LONG)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startActivity(
                        Intent(this@StartActivity, StartErrorActivity::class.java)
                    )
                }
                finish()
            }
        }
    }

    private suspend fun checkRootAccess(): Boolean {
        delay(500)
        return rootUtils.isRootAvailable()
    }

    private suspend fun checkBusyBox(): Boolean {
        delay(500)
        return rootUtils.isBusyboxAvailable()
    }

    private fun navigate() {
        val shortcutNames = Actions.entries.map { it.name }
        val nextIntent = Intent(this, MainActivity::class.java).apply {
            if (intent.action in shortcutNames) {
                putExtra(MainActivity.EXTRA_DESTINATION, intent.action)
                putExtras(intent.extras ?: Bundle())
            }
        }

        startActivity(nextIntent)
    }
}
