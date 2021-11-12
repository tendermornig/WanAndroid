package com.cl.wanandroid.module.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.cl.wanandroid.base.BaseFragment
import com.cl.wanandroid.base.list.XRecyclerView
import com.cl.wanandroid.base.list.base.BaseViewData
import com.cl.wanandroid.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()

    override fun initView() {
    }

    override fun loadData() {
        viewBinding.rvList.init(
            XRecyclerView.Config().setViewModel(viewModel).setOnItemChildViewClickListener(object :
                XRecyclerView.OnItemChildViewClickListener {
                override fun onItemChildViewClick(
                    parent: RecyclerView,
                    view: View,
                    viewData: BaseViewData<*>,
                    position: Int,
                    id: Long,
                    extra: Any?
                ) {
                    Toast.makeText(context, "条目点击: ${viewData.value}", Toast.LENGTH_SHORT).show()
                }
            })
        )
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