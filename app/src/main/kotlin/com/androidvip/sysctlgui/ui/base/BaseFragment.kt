package com.androidvip.sysctlgui.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.androidvip.sysctlgui.R

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseViewBindingFragment<Binding : ViewBinding>(
    private val inflate: Inflate<Binding>
) : Fragment() {
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected val recyclerViewColumns: Int
        get() = if (resources.getBoolean(R.bool.is_landscape)) 2 else 1

    private var _binding: Binding? = null
}
