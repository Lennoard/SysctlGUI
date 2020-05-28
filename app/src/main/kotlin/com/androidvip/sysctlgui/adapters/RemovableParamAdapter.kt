package com.androidvip.sysctlgui.adapters

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.activities.EditKernelParamActivity
import com.androidvip.sysctlgui.toast
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemovableParamAdapter(val context: Context, private val dataSet: MutableList<KernelParameter>) :
    RecyclerView.Adapter<RemovableParamAdapter.RemovableViewHolder>() {

    companion object {
        const val EXTRA_EDIT_SAVED_PARAM = "edit_saved_param"
    }

    class RemovableViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.listKernelParamName)
        var value: TextView = v.findViewById(R.id.listKernelParamValue)
        val popupIcon: ImageView = v.findViewById(R.id.listKernelParamPopUp)
        var removableView: LinearLayout = v.findViewById(R.id.removableKernelParamLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovableViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_removable_kernel_param, parent, false).apply {  }
        return RemovableViewHolder(v).apply {
            setIsRecyclable(false)
        }
    }

    override fun onBindViewHolder(holder: RemovableViewHolder, position: Int) {
        val kernelParam = dataSet[holder.adapterPosition]

        val popupMenu = PopupMenu(context, holder.popupIcon).apply {
            menuInflater.inflate(R.menu.popup_manage_params, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.popupEdit -> editParam(kernelParam)
                    R.id.popupRemove -> { removeItem(position, holder.removableView, true) }
                }
                true
            }
        }

        with (holder) {
            name.text = kernelParam.name
            value.text = kernelParam.value

            popupIcon.setOnClickListener { popupMenu.show() }
            itemView.setOnClickListener { editParam(kernelParam) }
            itemView.setOnLongClickListener {
                popupMenu.show()
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun updateData(newData: List<KernelParameter>) {
        dataSet.clear()
        dataSet.addAll(newData)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int, rowView: View, fakeGesture: Boolean = false) {
        if (position < 0 || position >= dataSet.size) return

        GlobalScope.launch(Dispatchers.IO) {
            val success = Prefs.removeParam(dataSet[position], context)

            withContext(Dispatchers.Main) {
                if (success) {
                    if (fakeGesture) {
                        YoYo.with(Techniques.SlideOutLeft)
                            .duration(450)
                            .interpolate(AnimationUtils.loadInterpolator(rowView.context, android.R.anim.accelerate_decelerate_interpolator))
                            .playOn(rowView)

                        Handler().postDelayed({
                            dataSet.removeAt(position)
                            notifyDataSetChanged()
                        }, 450)
                    } else {
                        dataSet.removeAt(position)
                        notifyDataSetChanged()
                    }
                } else {
                    if (fakeGesture) {
                        YoYo.with(Techniques.SlideInLeft)
                            .duration(450)
                            .interpolate(AnimationUtils.loadInterpolator(rowView.context, android.R.anim.accelerate_decelerate_interpolator))
                            .playOn(rowView)
                    }
                    context.toast(R.string.failed)
                }
            }
        }
    }

    private fun editParam(kernelParam: KernelParameter) {
        Intent(context, EditKernelParamActivity::class.java).apply {
            putExtra(KernelParamListAdapter.EXTRA_PARAM, kernelParam)
            putExtra(EXTRA_EDIT_SAVED_PARAM, true)
            context.startActivity(this)
        }
    }
}
