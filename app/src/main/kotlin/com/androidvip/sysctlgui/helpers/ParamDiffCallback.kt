package com.androidvip.sysctlgui.helpers

import androidx.recyclerview.widget.DiffUtil
import com.androidvip.sysctlgui.data.models.KernelParam

class ParamDiffCallback : DiffUtil.ItemCallback<KernelParam>() {
    override fun areItemsTheSame(oldItem: KernelParam, newItem: KernelParam): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: KernelParam, newItem: KernelParam): Boolean {
        return oldItem.name == newItem.name
            && oldItem.path == newItem.path
            && oldItem.value == newItem.value
            && oldItem.favorite == newItem.favorite
            && oldItem.taskerParam == newItem.taskerParam
    }
}
