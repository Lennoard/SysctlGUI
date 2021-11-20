package com.androidvip.sysctlgui.ui.export

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.androidvip.sysctlgui.data.models.SettingsItem
import com.androidvip.sysctlgui.databinding.ListItemSettingsBinding
import com.androidvip.sysctlgui.helpers.OnSettingsItemClickedListener
import com.androidvip.sysctlgui.helpers.SettingsItemDiffCallback
import com.androidvip.sysctlgui.ui.base.BaseViewHolder

class ExportOptionsItemAdapter(
    private val itemClickedListener: OnSettingsItemClickedListener
) : ListAdapter<SettingsItem, BaseViewHolder<*>>(SettingsItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemSettingsBinding.inflate(
            inflater, parent, false
        )
        return HomeItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is HomeItemViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    inner class HomeItemViewHolder(
        private val binding: ListItemSettingsBinding
    ) : BaseViewHolder<SettingsItem>(binding) {
        override fun bind(item: SettingsItem, position: Int) {
            binding.item = item
            binding.position = position
            binding.onSettingsItemClickedListener = itemClickedListener
            binding.executePendingBindings()
        }
    }
}
