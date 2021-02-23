package com.androidvip.sysctlgui.ui.params.browse

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ListItemKernelFileBrowserBinding
import com.androidvip.sysctlgui.helpers.BaseViewHolder
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.ParamDiffCallback
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
            holder.bind(getItem(position))
        }
    }

    fun updateData(newList: MutableList<KernelParam>) {
        submitList(newList)
    }

    inner class ParamBrowserViewHolder(
        private val binding: ListItemKernelFileBrowserBinding
    ) : BaseViewHolder<KernelParam>(binding) {
        override fun bind(item: KernelParam) {
            val kernelFile = File(item.path)
            binding.param = item

            if (kernelFile.isDirectory) {
                with(binding) {
                    name.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    name.setTextColor(Color.WHITE)
                    icon.setImageResource(R.drawable.ic_folder_outline)
                    icon.setBackgroundResource(R.drawable.circle_folder)
                    icon.setColorFilter(
                        ContextCompat.getColor(binding.icon.context, R.color.colorAccentLight),
                        PorterDuff.Mode.SRC_IN
                    )

                    listKernelBrowserLayout.setOnClickListener {
                        directoryChangedListener.onDirectoryChanged(kernelFile)
                    }
                }
            } else {
                with(binding) {
                    name.setTextColor(Color.parseColor("#99FFFFFF")) // 60% white
                    icon.setImageResource(R.drawable.ic_file_outline)
                    icon.setBackgroundResource(R.drawable.circle_file)
                    icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

                    listKernelBrowserLayout.setOnClickListener {
                        paramItemClickedListener.onParamItemClicked(item)
                    }
                }
            }

            binding.executePendingBindings()
        }
    }
}
