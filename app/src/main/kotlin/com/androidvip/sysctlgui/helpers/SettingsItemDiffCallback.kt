package com.androidvip.sysctlgui.helpers

import androidx.recyclerview.widget.DiffUtil
import com.androidvip.sysctlgui.data.models.SettingsItem

internal object SettingsItemDiffCallback : DiffUtil.ItemCallback<SettingsItem>() {
    override fun areItemsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
        return oldItem.titleRes == newItem.titleRes
    }

    override fun areContentsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
        return oldItem == newItem
    }
}
