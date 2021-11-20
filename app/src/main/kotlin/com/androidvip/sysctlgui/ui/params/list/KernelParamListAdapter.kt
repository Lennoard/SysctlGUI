package com.androidvip.sysctlgui.ui.params.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ListItemKernelParamListBinding
import com.androidvip.sysctlgui.helpers.ParamDiffCallback
import com.androidvip.sysctlgui.ui.base.BaseViewHolder
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener

class KernelParamListAdapter(
    private val paramItemClickedListener: OnParamItemClickedListener
) : ListAdapter<KernelParam, BaseViewHolder<*>>(ParamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemKernelParamListBinding.inflate(
            inflater, parent, false
        )
        return ParamListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is ParamListViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    fun updateData(newList: MutableList<KernelParam>) {
        submitList(newList)
    }

    inner class ParamListViewHolder(
        private val binding: ListItemKernelParamListBinding
    ) : BaseViewHolder<KernelParam>(binding) {
        override fun bind(item: KernelParam, position: Int) {
            binding.param = item
            binding.onParamItemClickedListener = paramItemClickedListener
            binding.executePendingBindings()
        }
    }
}
