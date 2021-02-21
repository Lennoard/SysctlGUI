package com.androidvip.sysctlgui.ui.settings

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.utils.RootUtils
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.receivers.BootReceiver
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val currCommitMode = prefs.getString(Prefs.COMMIT_MODE, "sysctl")
        val commitModePref = findPreference<Preference?>(Prefs.COMMIT_MODE)
        commitModePref?.summary = if (currCommitMode == "sysctl")
            "Use sysctl -w"
        else
            "Use echo 'value' > /proc/sys/…"

        val startupDelay = prefs.getInt(Prefs.START_UP_DELAY, 0)
        val startupDelayPref = findPreference<Preference?>(Prefs.START_UP_DELAY)

        startupDelayPref?.summary = if (startupDelay > 0) {
            getString(R.string.startup_delay_sum, startupDelay)
        } else {
            getString(R.string.startup_delay_disabled)
        }

        val useBusyboxPref = findPreference(Prefs.USE_BUSYBOX) as SwitchPreferenceCompat?
        lifecycleScope.launch {
            if (RootUtils.isBusyboxAvailable()) {
                useBusyboxPref?.isEnabled = true
            } else {
                useBusyboxPref?.isChecked = false
                useBusyboxPref?.isEnabled = false
            }
        }

        commitModePref?.onPreferenceChangeListener = this
        startupDelayPref?.onPreferenceChangeListener = this
        findPreference<Preference?>(Prefs.RUN_ON_START_UP)?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        when (preference?.key) {
            Prefs.RUN_ON_START_UP -> {
                val receiver = ComponentName(requireContext(), BootReceiver::class.java)
                val state = if (newValue == true) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                requireContext().packageManager.setComponentEnabledSetting(
                    receiver,
                    state,
                    PackageManager.DONT_KILL_APP
                )
            }

            Prefs.COMMIT_MODE -> {
                preference.summary = if (newValue == "sysctl")
                    "Use sysctl -w"
                else
                    "Use echo 'value' > /proc/sys/…"
            }

            Prefs.START_UP_DELAY -> {
                val selectedValue = (newValue as? Int) ?: 0

                preference.summary = if (selectedValue > 0) {
                    getString(R.string.startup_delay_sum, selectedValue)
                } else {
                    getString(R.string.startup_delay_disabled)
                }
            }
        }

        return true
    }
}