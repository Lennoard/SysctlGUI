package com.androidvip.sysctlgui.services.tiles

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.helpers.StartUpServiceToggle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StartUpTileService : TileService(), KoinComponent {
    private val prefs: AppPrefs by inject()
    private val rootUtils: RootUtils by inject()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)


    override fun onStartListening() {
        super.onStartListening()

        serviceScope.launch {
            qsTile.apply {
                if (rootUtils.isRootAvailable()) {
                    state = if (isStartUpEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                } else {
                    state = Tile.STATE_UNAVAILABLE
                    label = resources.getString(R.string.tile_toggle_start_up_no_root_access_label)
                }
                updateTile()
            }
        }
    }

    override fun onClick() {
        super.onClick()

        serviceScope.launch {
            if (!rootUtils.isRootAvailable()) {
                Toast.makeText(
                    this@StartUpTileService,
                    resources.getString(R.string.tile_toggle_start_up_no_root_access_toast),
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            toggleService(isStartUpEnabled().not())

            qsTile.apply {
                state = if (isStartUpEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                updateTile()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun isStartUpEnabled() = prefs.runOnStartUp

    private fun toggleService(enabled: Boolean) {
        prefs.runOnStartUp = enabled
        StartUpServiceToggle.toggleStartUpService(this, enabled)
    }
}
