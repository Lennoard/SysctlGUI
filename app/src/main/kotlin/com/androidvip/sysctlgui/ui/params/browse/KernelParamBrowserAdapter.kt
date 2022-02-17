package com.androidvip.sysctlgui.ui.params.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ListItemKernelFileBrowserBinding
import com.androidvip.sysctlgui.helpers.ParamDiffCallback
import com.androidvip.sysctlgui.ui.base.BaseViewHolder
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import java.io.File

class KernelParamBrowserAdapter(
    private val directoryChangedListener: DirectoryChangedListener,
    private val paramItemClickedListener: OnParamItemClickedListener
) : ListAdapter<KernelParam, BaseViewHolder<*>>(ParamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamBrowserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemKernelFileBrowserBinding.inflate(
            inflater, parent, false
        )
        return ParamBrowserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is ParamBrowserViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    fun updateData(newList: List<KernelParam>) {
        submitList(newList)
    }

    inner class ParamBrowserViewHolder(
        private val binding: ListItemKernelFileBrowserBinding
    ) : BaseViewHolder<KernelParam>(binding) {
        override fun bind(item: KernelParam, position: Int) {
            val kernelFile = File(item.path)
            binding.param = item
            binding.kernelFile = kernelFile
            binding.paramListener = paramItemClickedListener
            binding.directoryListener = directoryChangedListener

            binding.executePendingBindings()
        }
    }
}

interface DirectoryChangedListener {
    fun onDirectoryChanged(newDir: File)
}
