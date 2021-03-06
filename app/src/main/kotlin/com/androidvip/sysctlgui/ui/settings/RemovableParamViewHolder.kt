package com.androidvip.sysctlgui.ui.settings

import android.widget.PopupMenu
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ListItemRemovableKernelParamBinding
import com.androidvip.sysctlgui.ui.base.BaseViewHolder
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener

class RemovableParamViewHolder(
    val binding: ListItemRemovableKernelParamBinding,
    private val popUpMenuItemSelectedListener: OnPopUpMenuItemSelectedListener,
    private val paramItemClickedListener: OnParamItemClickedListener
) : BaseViewHolder<KernelParam>(binding) {
    override fun bind(item: KernelParam) {
        binding.param = item

        val popupMenu = PopupMenu(binding.popUpIcon.context, binding.popUpIcon).apply {
            menuInflater.inflate(R.menu.popup_manage_params, menu)
            setOnMenuItemClickListener {
                popUpMenuItemSelectedListener.onPopUpMenuItemSelected(
                    item, it.itemId, binding.removableLayout
                )
                true
            }
        }

        with (binding) {
            popUpIcon.setOnClickListener { popupMenu.show() }
            itemView.setOnClickListener {
                paramItemClickedListener.onParamItemClicked(item, removableLayout)
            }
            itemView.setOnLongClickListener {
                popupMenu.show()
                true
            }
        }

        binding.executePendingBindings()
    }
}
