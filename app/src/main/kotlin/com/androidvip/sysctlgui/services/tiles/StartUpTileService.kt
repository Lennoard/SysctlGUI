package com.androidvip.sysctlgui.services.tiles

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.helpers.StartUpServiceToggle
import com.topjohnwu.superuser.Shell
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@RequiresApi(Build.VERSION_CODES.N)
class StartUpTileService : TileService(), KoinComponent {
    private val prefs: AppPrefs by inject()

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

    private fun isStartUpEnabled() = prefs.runOnStartUp

    private fun toggleService(enabled: Boolean) {
        prefs.runOnStartUp = enabled
        StartUpServiceToggle.toggleStartUpService(this, enabled)
    }
}
