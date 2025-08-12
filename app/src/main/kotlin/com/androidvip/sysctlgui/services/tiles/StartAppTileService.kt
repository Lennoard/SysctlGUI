package com.androidvip.sysctlgui.services.tiles

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.service.quicksettings.PendingIntentActivityWrapper
import androidx.core.service.quicksettings.TileServiceCompat
import com.androidvip.sysctlgui.ui.start.StartActivity


class StartAppTileService : TileService() {

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        qsTile.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }

        val intent = Intent(this, StartActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val wrapper = PendingIntentActivityWrapper(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT,
            false
        )

        TileServiceCompat.startActivityAndCollapse(this, wrapper)
    }
}
