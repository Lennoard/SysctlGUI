package com.androidvip.sysctlgui.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.androidvip.sysctlgui.data.models.HomeItem
import com.androidvip.sysctlgui.databinding.ListItemHomeItemBinding
import com.androidvip.sysctlgui.ui.base.BaseViewHolder

class HomeItemAdapter(
    private val itemClickedListener: OnHomeItemClickedListener
) : ListAdapter<HomeItem, BaseViewHolder<*>>(HomeItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemHomeItemBinding.inflate(
            inflater, parent, false
        )
        return HomeItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is HomeItemViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    inner class HomeItemViewHolder(
        private val binding: ListItemHomeItemBinding
    ) : BaseViewHolder<HomeItem>(binding) {
        override fun bind(item: HomeItem, position: Int) {
            binding.item = item
            binding.position = position
            binding.onHomeItemClickedListener = itemClickedListener
            binding.executePendingBindings()
        }
    }

    private object HomeItemDiffCallback : DiffUtil.ItemCallback<HomeItem>() {
        override fun areItemsTheSame(oldItem: HomeItem, newItem: HomeItem): Boolean {
            return oldItem.titleRes == newItem.titleRes
        }

        override fun areContentsTheSame(oldItem: HomeItem, newItem: HomeItem): Boolean {
            return oldItem == newItem
        }
    }

    interface OnHomeItemClickedListener {
        fun onHomeItemClicked(item: HomeItem, position: Int)
    }
}
