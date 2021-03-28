package com.androidvip.sysctlgui.services.tiles

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.helpers.StartUpServiceToggle
import com.androidvip.sysctlgui.prefs.Prefs
import com.topjohnwu.superuser.Shell

@RequiresApi(Build.VERSION_CODES.N)
class StartUpTileService : TileService() {

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onStartListening() {
        super.onStartListening()

        qsTile.apply {
            if (Shell.rootAccess()) {
                state = if (isStartUpEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            } else {
                state = Tile.STATE_UNAVAILABLE
                label = resources.getString(R.string.tile_toggle_start_up_no_root_access_label)
            }
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        if (!Shell.rootAccess()) {
            Toast.makeText(
                this,
                resources.getString(R.string.tile_toggle_start_up_no_root_access_toast),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        toggleService(isStartUpEnabled().not())

        qsTile.apply {
            state = if (isStartUpEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }

    private fun isStartUpEnabled() = sharedPreferences
        .getBoolean(Prefs.RUN_ON_START_UP, false)

    private fun toggleService(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(Prefs.RUN_ON_START_UP, enabled).commit()
        StartUpServiceToggle.toggleStartUpService(this, enabled)
    }
}