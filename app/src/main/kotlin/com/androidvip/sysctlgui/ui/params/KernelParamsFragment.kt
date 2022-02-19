package com.androidvip.sysctlgui.ui.params

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.databinding.FragmentKernelParamsBinding
import com.androidvip.sysctlgui.ui.base.BaseViewBindingFragment
import com.androidvip.sysctlgui.ui.params.browse.KernelParamBrowseFragment
import com.androidvip.sysctlgui.ui.params.list.KernelParamListFragment
import com.google.android.material.tabs.TabLayoutMediator

class KernelParamsFragment : BaseViewBindingFragment<FragmentKernelParamsBinding>(
    FragmentKernelParamsBinding::inflate
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() = with(binding) {
        val adapter = ParamPagerAdapter(childFragmentManager, lifecycle).apply {
            addFragment(KernelParamBrowseFragment(), getString(R.string.browse))
            addFragment(KernelParamListFragment(), getString(R.string.param_list))
        }

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
        }.attach()
    }

    private class ParamPagerAdapter(
        manager: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(manager, lifecycle) {
        private val fragments = mutableListOf<Fragment>()
        private val titles = mutableListOf<String>()

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        fun getTitle(position: Int) = titles[position]
    }
}
