package com.androidvip.sysctlgui.ui.params

import androidx.recyclerview.widget.DiffUtil
import com.androidvip.sysctlgui.data.models.KernelParam

class ParamDiffCallback : DiffUtil.ItemCallback<KernelParam>() {
    override fun areItemsTheSame(oldItem: KernelParam, newItem: KernelParam): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: KernelParam, newItem: KernelParam): Boolean {
        return oldItem == newItem
    }
}