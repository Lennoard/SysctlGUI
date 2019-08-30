package com.androidvip.sysctlgui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.Prefs
import com.androidvip.sysctlgui.R

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

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val currCommitMode = PreferenceManager.getDefaultSharedPreferences(context!!).getString(Prefs.COMMIT_MODE, "sysctl")
            val commitModePref =findPreference(Prefs.COMMIT_MODE)
            commitModePref?.summary = if (currCommitMode == "sysctl")
                "Use sysctl -w"
            else
                "Use echo 'value' > /proc/sys/…"

            commitModePref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                commitModePref?.summary = if (newValue == "sysctl")
                    "Use sysctl -w"
                else
                    "Use echo 'value' > /proc/sys/…"
                true
            }
        }
    }
}