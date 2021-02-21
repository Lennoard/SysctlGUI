package com.androidvip.sysctlgui.ui.paramlist

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.utils.RootUtils
import com.androidvip.sysctlgui.ui.EditKernelParamActivity
import com.androidvip.sysctlgui.ui.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class KernelParamListAdapter(
    private val activityRef: WeakReference<BaseActivity>,
    private val dataSet: MutableList<KernelParameter>
) : RecyclerView.Adapter<KernelParamListAdapter.ViewHolder>() {

    companion object {
        const val EXTRA_PARAM = "kernel_param"
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.listKernelParamName)
        var value: TextView = v.findViewById(R.id.listKernelParamValue)
        var itemLayout: LinearLayout = v.findViewById(R.id.listKernelParamLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(activityRef.get()).inflate(
            R.layout.list_item_kernel_param_list,
            parent,
            false
        )
        return ViewHolder(
            v
        ).apply { setIsRecyclable(false) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activityRef.get()
        val kernelParam = dataSet[holder.adapterPosition]

        holder.name.text = kernelParam.name
        holder.itemLayout.setOnClickListener(null)

        activity?.launch {
            val paramValue = getParamValue(kernelParam.path)

            kernelParam.value = paramValue
            holder.value.text = paramValue
            holder.itemLayout.setOnClickListener {
                Intent(activity, EditKernelParamActivity::class.java).apply {
                    putExtra(EXTRA_PARAM, kernelParam)
                    activity.startActivity(this)
                }
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

    private suspend fun getParamValue(path: String) = withContext(Dispatchers.IO) {
        RootUtils.executeWithOutput("cat $path", "")
    }
}
