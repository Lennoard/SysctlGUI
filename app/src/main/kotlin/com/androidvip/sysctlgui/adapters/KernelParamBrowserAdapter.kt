package com.androidvip.sysctlgui.adapters

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.RootUtils
import com.androidvip.sysctlgui.activities.DirectoryChangedListener
import com.androidvip.sysctlgui.activities.EditKernelParamActivity
import com.androidvip.sysctlgui.activities.base.BaseActivity
import com.androidvip.sysctlgui.prefs.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference

class KernelParamBrowserAdapter(
    allFiles: Array<File>,
    private val activityRef: WeakReference<BaseActivity>,
    private val directoryChangedListener: DirectoryChangedListener
) : RecyclerView.Adapter<KernelParamBrowserAdapter.ViewHolder>() {

    private val dataSet = mutableListOf<File>()
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(activityRef.get())
    }

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
        val v = LayoutInflater.from(activityRef.get()).inflate(
            R.layout.list_item_kernel_file_browser,
            parent,
            false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activityRef.get()
        val kernelFile = dataSet[position]
        val kernelParam = KernelParameter(path = kernelFile.absolutePath).apply {
            setNameFromPath(this.path)
        }

        if (kernelFile.isDirectory) {
            with (holder) {
                name.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                name.setTextColor(Color.WHITE)
                icon.setImageResource(R.drawable.ic_folder_outline)
                icon.setBackgroundResource(R.drawable.circle_folder)
                icon.setColorFilter(ContextCompat.getColor(activityRef.get()!!,
                    R.color.colorAccentLight
                ), PorterDuff.Mode.SRC_IN)
            }
        } else {
            with (holder) {
                name.setTextColor(Color.parseColor("#99FFFFFF")) // 60% white
                icon.setImageResource(R.drawable.ic_file_outline)
                icon.setBackgroundResource(R.drawable.circle_file)
                icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }
        }

        holder.name.text = kernelFile.nameWithoutExtension
        holder.itemLayout.setOnClickListener(null)

        activity?.launch {
            val paramValue = getParamValue(kernelFile.path)
            kernelParam.value = paramValue

            if (kernelFile.isDirectory) {
                holder.itemLayout.setOnClickListener {
                    directoryChangedListener.onDirectoryChanged(kernelFile)
                }
            } else {
                holder.itemLayout.setOnClickListener {
                    Intent(activity, EditKernelParamActivity::class.java).apply {
                        putExtra(EXTRA_PARAM, kernelParam)
                        activity.startActivity(this)
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

        if (prefs.getBoolean(Prefs.LIST_FOLDERS_FIRST, true)) {
            dataSet.sortWith(Comparator { f1, f2 ->
                f2.isDirectory.compareTo(f1.isDirectory)
            })
        }
    }

    private suspend fun getParamValue(path: String) = withContext(Dispatchers.Default) {
        RootUtils.executeWithOutput("cat $path", "")
    }
}
