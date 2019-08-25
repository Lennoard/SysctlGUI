package com.androidvip.sysctlgui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class KernelParamBrowserAdapter(
    allFiles: Array<File>,
    private val context: Context,
    private val directoryChangedListener: DirectoryChangedListener
) : RecyclerView.Adapter<KernelParamBrowserAdapter.ViewHolder>() {

    private val dataSet = mutableListOf<File>()

    companion object {
        const val EXTRA_PARAM = "kernel_param"
    }

    init {
        filterAndSortByName(allFiles)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.listKernelBrowserName)
        var icon: ImageView = v.findViewById(R.id.listKernelBrowserIcon)
        var itemLayout: LinearLayout = v.findViewById(R.id.listKernelBrowserLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_kernel_file_browser, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kernelFile = dataSet[position]
        val kernelParam = KernelParameter(path = kernelFile.absolutePath).apply {
            setParamFromPath(this.path)
        }

        if (kernelFile.isDirectory) {
            holder.name.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            holder.name.setTextColor(Color.WHITE)
            holder.icon.setImageResource(R.drawable.ic_folder_outline)
            holder.icon.setBackgroundResource(R.drawable.circle_folder)
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.colorAccentLight), PorterDuff.Mode.SRC_IN)
        } else {
            holder.name.setTextColor(Color.parseColor("#99FFFFFF")) // 60% white
            holder.icon.setImageResource(R.drawable.ic_file_outline)
            holder.icon.setBackgroundResource(R.drawable.circle_file)
            holder.icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }

        holder.name.text = kernelFile.nameWithoutExtension
        holder.itemLayout.setOnClickListener(null)

        GlobalScope.launch {
            val paramValue = getParamValue(kernelFile.path)
            withContext(Dispatchers.Main) {
                kernelParam.value = paramValue

                if (kernelFile.isDirectory) {
                    holder.itemLayout.setOnClickListener {
                        directoryChangedListener.onDirectoryChanged(kernelFile)
                    }
                } else {
                    holder.itemLayout.setOnClickListener {
                        Intent(context, EditKernelParamActivity::class.java).apply {
                            putExtra(EXTRA_PARAM, kernelParam)
                            context.startActivity(this)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun updateData(newData: Array<File>) {
        filterAndSortByName(newData)
        notifyDataSetChanged()
    }

    private fun filterAndSortByName(files: Array<File>) {
        dataSet.clear()
        files.forEach {
            if ((it.exists() || it.isDirectory)) {
                dataSet.add(it)
            }
        }

        dataSet.sortWith(Comparator { f1, f2 ->
            f2.isDirectory.compareTo(f1.isDirectory)
        })
    }

    private suspend fun getParamValue(path: String) = withContext(Dispatchers.Default) {
        RootUtils.executeWithOutput("cat $path", "")
    }
}
