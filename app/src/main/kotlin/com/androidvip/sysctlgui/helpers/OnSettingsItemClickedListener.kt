package com.androidvip.sysctlgui.helpers

import com.androidvip.sysctlgui.data.models.SettingsItem

interface OnSettingsItemClickedListener {
    fun onSettingsItemClicked(item: SettingsItem, position: Int)
}
