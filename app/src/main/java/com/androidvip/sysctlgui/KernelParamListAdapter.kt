package com.androidvip.sysctlgui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KernelParamListAdapter(private val context: Context, private val dataSet: MutableList<KernelParam>) : RecyclerView.Adapter<KernelParamListAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.listKernelParamName)
        var value: TextView = v.findViewById(R.id.listKernelParamValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_kernel_param, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (path, param) = dataSet[position]
        holder.name.text = param

        GlobalScope.launch {
            val paramValue = getParamValue(path)
            withContext(Dispatchers.Main) {
                holder.value.text = paramValue
                dataSet[position].value = paramValue
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun updateData(newData: List<KernelParam>) {
        dataSet.clear()
        dataSet.addAll(newData)
        notifyDataSetChanged()
    }

    private suspend fun getParamValue(path: String) = withContext(Dispatchers.Default) {
        RootUtils.executeWithOutput("cat $path", "")
    }
}
