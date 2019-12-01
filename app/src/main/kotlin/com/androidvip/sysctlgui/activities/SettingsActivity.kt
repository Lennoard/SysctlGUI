package com.androidvip.sysctlgui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.androidvip.sysctlgui.Prefs
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.RootUtils
import com.androidvip.sysctlgui.runSafeOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with (supportFragmentManager.beginTransaction()) {
            replace(R.id.settingsFragmentHolder, SettingsFragment())
            commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)

            val currCommitMode = prefs.getString(Prefs.COMMIT_MODE, "sysctl")
            val commitModePref = findPreference<Preference?>(Prefs.COMMIT_MODE)
            commitModePref?.summary = if (currCommitMode == "sysctl")
                "Use sysctl -w"
            else
                "Use echo 'value' > /proc/sys/…"

            commitModePref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                commitModePref?.summary = if (newValue == "sysctl")
                    "Use sysctl -w"
                else
                    "Use echo 'value' > /proc/sys/…"
                true
            }

            val useBusyboxPref = findPreference(Prefs.USE_BUSYBOX) as SwitchPreferenceCompat?

            GlobalScope.launch(Dispatchers.IO) {
                val isBusyboxAvailable = RootUtils.isBusyboxAvailable()
                activity.runSafeOnUiThread {
                    if (isBusyboxAvailable) {
                        useBusyboxPref?.isEnabled = true
                    } else {
                        useBusyboxPref?.isChecked = false
                        useBusyboxPref?.isEnabled = false
                    }
                }
            }
        }
    }
}