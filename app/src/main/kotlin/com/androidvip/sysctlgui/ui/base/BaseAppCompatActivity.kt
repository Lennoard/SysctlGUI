package com.androidvip.sysctlgui.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.androidvip.sysctlgui.design.DesignStyles
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import org.koin.android.ext.android.inject

/**
 * Base activity that uses AppCompat for theming
 * TODO: Temporary until 100% compose
 */
abstract class BaseAppCompatActivity : AppCompatActivity() {
    protected val prefs by inject<AppPrefs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (prefs.forceDark) {
            setTheme(DesignStyles.AppTheme_ForceDark)
        }
    }
}
