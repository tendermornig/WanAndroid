package com.cl.wanandroid.module.home

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.cl.wanandroid.base.BaseFragment
import com.cl.wanandroid.base.list.XRecyclerView
import com.cl.wanandroid.base.list.loadmore.LoadMoreRecyclerView
import com.cl.wanandroid.databinding.FragmentHomeBinding
import com.cl.wanandroid.module.main.MainActivity

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()

    override fun initView() {
    }

    override fun loadData() {
        viewBinding.rvList.init(XRecyclerView.Config().setViewModel(viewModel))
    }

    

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}