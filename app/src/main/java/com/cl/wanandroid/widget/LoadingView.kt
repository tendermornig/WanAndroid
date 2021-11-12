package com.cl.wanandroid.widget

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.cl.wanandroid.R
import com.cl.wanandroid.WanAndroidApplication
import com.cl.wanandroid.databinding.ViewLoadingBinding

/**
 *  加载控件
 */
class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val viewBinding = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true).apply {
        val flag =
            WanAndroidApplication.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (flag == Configuration.UI_MODE_NIGHT_YES) {
            viewLoading.setViewColor(R.color.invert)
        } else {
            viewLoading.setViewColor(R.color.main)
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.VISIBLE) {
            viewBinding.viewLoading.startAnim()
        } else {
            viewBinding.viewLoading.stopAnim()
        }
    }

    override fun onDetachedFromWindow() {
        viewBinding.viewLoading.stopAnim()
        super.onDetachedFromWindow()
    }
}