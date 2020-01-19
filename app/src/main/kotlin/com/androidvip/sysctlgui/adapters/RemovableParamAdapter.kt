package com.androidvip.sysctlgui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.Prefs
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.activities.EditKernelParamActivity
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
        var removableView: LinearLayout = v.findViewById(R.id.removableKernelParamLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovableViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_removable_kernel_param, parent, false)
        return RemovableViewHolder(v)
    }

    override fun onBindViewHolder(holder: RemovableViewHolder, position: Int) {
        val kernelParam = dataSet[holder.adapterPosition]

        holder.name.text = kernelParam.name
        holder.value.text = kernelParam.value

        holder.removableView.setOnClickListener {
            Intent(context, EditKernelParamActivity::class.java).apply {
                putExtra(KernelParamListAdapter.EXTRA_PARAM, kernelParam)
                putExtra(EXTRA_EDIT_SAVED_PARAM, true)
                context.startActivity(this)
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

    fun deleteItem(position: Int) {
        if (position < 0 || position >= dataSet.size) return

        GlobalScope.launch(Dispatchers.IO) {
            val success = Prefs.removeParam(dataSet[position], context)

            withContext(Dispatchers.Main) {
                if (success) {
                    dataSet.removeAt(position)
                    notifyItemRemoved(position)
                } else {
                    Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
