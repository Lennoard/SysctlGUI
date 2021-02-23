package com.androidvip.sysctlgui.ui.params.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ListItemKernelParamListBinding
import com.androidvip.sysctlgui.helpers.BaseViewHolder
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.ParamDiffCallback
import com.androidvip.sysctlgui.utils.RootUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KernelParamListAdapter(
    private val paramItemClickedListener: OnParamItemClickedListener,
    private val coroutineScope: CoroutineScope
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
            holder.bind(getItem(position))
        }
    }

    fun updateData(newList: MutableList<KernelParam>) {
        submitList(newList)
    }

    inner class ParamListViewHolder(
        private val binding: ListItemKernelParamListBinding
    ) : BaseViewHolder<KernelParam>(binding) {
        override fun bind(item: KernelParam) {
            binding.param = item
            binding.executePendingBindings()

            binding.paramLayout.setOnClickListener {
                paramItemClickedListener.onParamItemClicked(item)
            }

            coroutineScope.launch {
                binding.paramValue.text = getParamValue(item)
            }
        }
    }

    private suspend fun getParamValue(param: KernelParam): String {
        return RootUtils.executeWithOutput("cat ${param.path}", "")
    }
}
