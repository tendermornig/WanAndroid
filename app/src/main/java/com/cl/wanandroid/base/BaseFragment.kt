package com.cl.wanandroid.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding>(val inflater: (inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> VB) :
    Fragment() {

    protected lateinit var viewBinding: VB
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = this.inflater(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initView()
        loadData()
    }

    /**
     * 绑定事件
     */
    protected abstract fun initView()

    /**
     * 加载数据
     */
    protected abstract fun loadData()
}