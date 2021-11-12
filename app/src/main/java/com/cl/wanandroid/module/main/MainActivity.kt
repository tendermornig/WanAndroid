package com.cl.wanandroid.module.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cl.wanandroid.R
import com.cl.wanandroid.base.BaseActivity
import com.cl.wanandroid.databinding.ActivityMainBinding
import com.cl.wanandroid.module.home.HomeFragment
import kotlin.math.abs

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun initView() {
        window.setBackgroundDrawable(ColorDrawable(getThemeColor(R.attr.colorBackground)))

        viewBinding.vp.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return HomeFragment()
            }
        }
    }

    private var startX = 0
    private var startY = 0

    /**
     * 重写此方法 用于解决ViewPager2切换过于灵敏的问题
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action?.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toInt()
                startY = ev.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                if (viewBinding.vp.isUserInputEnabled) {
                    val endX = ev.x.toInt()
                    val endY = ev.y.toInt()
                    val disX = abs(endX - startX)
                    val disY = abs(endY - startY)
                    if (disX < disY) {
                        /**
                         * 水平滑动的距离小于垂直的滑动距离 判定此时在下拉刷新等垂直操作
                         * 禁止ViewPager2滑动切换
                         */
                        viewBinding.vp.isUserInputEnabled = false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                startX = 0
                startY = 0
                //恢复ViewPager2的滑动
                viewBinding.vp.isUserInputEnabled = true
            }
            MotionEvent.ACTION_CANCEL -> {
                startX = 0
                startY = 0
                //恢复ViewPager2的滑动
                viewBinding.vp.isUserInputEnabled = true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun swipeBackEnable() = false

    override fun loadData() {
    }

    private fun getThemeColor(id: Int): Int {
        val typedArray = obtainStyledAttributes(intArrayOf(id))
        val color = typedArray.getColor(0, Color.TRANSPARENT)
        typedArray.recycle()
        return color
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}