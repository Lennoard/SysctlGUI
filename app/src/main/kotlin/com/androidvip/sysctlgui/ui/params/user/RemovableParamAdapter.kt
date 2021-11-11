package com.androidvip.sysctlgui.ui.params.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ListItemRemovableKernelParamBinding
import com.androidvip.sysctlgui.helpers.ParamDiffCallback
import com.androidvip.sysctlgui.ui.base.BaseViewHolder
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.OnPopUpMenuItemSelectedListener

class RemovableParamAdapter(
    private val onPopUpMenuItemSelectedListener: OnPopUpMenuItemSelectedListener,
    private val onParamItemClickedListener: OnParamItemClickedListener,
    var onRemoveRequestedListener: OnRemoveRequestedListener
) : ListAdapter<KernelParam, BaseViewHolder<*>>(ParamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovableParamViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemRemovableKernelParamBinding.inflate(
            inflater, parent, false
        )
        return RemovableParamViewHolder(
            binding, onPopUpMenuItemSelectedListener, onParamItemClickedListener
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is RemovableParamViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    fun updateData(newList: List<KernelParam>) {
        submitList(newList)
    }

    interface OnRemoveRequestedListener {
        fun onRemoveRequested(
            kernelParam: KernelParam,
            fakeGesture: Boolean,
            removableLayout: View
        )
    }

    companion object {
        const val EXTRA_EDIT_SAVED_PARAM = "edit_saved_param"
        const val EXTRA_PARAM = "param"
    }
}
