package com.cl.wanandroid.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import me.imid.swipebacklayout.lib.app.SwipeBackActivity

abstract class BaseActivity<VB: ViewBinding>(val inflater: (inflater: LayoutInflater) -> VB) : SwipeBackActivity() {

    protected lateinit var viewBinding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = inflater(layoutInflater)
        setContentView(viewBinding.root)
        setSwipeBackEnable(swipeBackEnable())
        initView()
        loadData()
    }

    /**
     * 默认开启左滑返回,如果需要禁用,请重写此方法
     */
    protected open fun swipeBackEnable()  = true

    /**
     * 初始化控件
     */
    protected abstract fun initView()

    /**
     * 绑定数据
     */
    protected abstract fun loadData()
}