package com.androidvip.sysctlgui.services.tiles

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.androidvip.sysctlgui.ui.start.StartActivity

@RequiresApi(Build.VERSION_CODES.N)
class StartAppTileService : TileService() {
    override fun onClick() {
        super.onClick()
        qsTile.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
        startActivityAndCollapse(
            Intent(this, StartActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }
}