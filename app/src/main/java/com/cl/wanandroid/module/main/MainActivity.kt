package com.cl.wanandroid.module.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.cl.wanandroid.R
import com.cl.wanandroid.base.BaseActivity
import com.cl.wanandroid.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun initView() {
        window.setBackgroundDrawable(ColorDrawable(getThemeColor(R.attr.colorBackground)))
    }

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