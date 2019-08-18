package com.androidvip.sysctlgui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.stericson.RootShell.RootShell
import com.stericson.RootTools.RootTools
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryLight)
        }

        GlobalScope.launch {
            val isRootAccessGiven = checkRootAccess()
            withContext(Dispatchers.Main) {
                splashStatusText.setText(R.string.splash_status_checking_busybox)
            }

            val isBusyBoxAvailable = checkBusyBox()
            withContext(Dispatchers.Main) {
                if (isRootAccessGiven) {
                    if (isBusyBoxAvailable) {
                       startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    } else {
                        splashProgress.visibility = View.GONE
                        AlertDialog.Builder(this@SplashActivity)
                            .setTitle(R.string.error)
                            .setMessage("Busybox not found")
                            .setPositiveButton("OK") { _, _ ->  }
                            .show()
                    }
                } else {
                    splashProgress.visibility = View.GONE
                    AlertDialog.Builder(this@SplashActivity)
                        .setTitle(R.string.error)
                        .setMessage("Root access not found")
                        .setPositiveButton("OK") { _, _ ->  }
                        .show()
                }
            }
        }
    }

    private suspend fun checkRootAccess() = withContext(Dispatchers.IO) {
        delay(500)
        RootTools.isAccessGiven(6000, 2)
    }

    private suspend fun checkBusyBox() = withContext(Dispatchers.IO) {
        delay(500)
        RootShell.isBusyboxAvailable()
    }
}
