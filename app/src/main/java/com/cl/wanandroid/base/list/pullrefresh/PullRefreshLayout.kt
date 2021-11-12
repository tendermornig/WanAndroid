package com.cl.wanandroid.base.list.pullrefresh

import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrUIHandler
import `in`.srain.cube.views.ptr.indicator.PtrIndicator
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import com.cl.wanandroid.R
import com.cl.wanandroid.databinding.ViewRecyclerHeaderBinding
import com.cl.wanandroid.util.v

class PullRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PtrFrameLayout(context, attrs, defStyleAttr), PtrUIHandler {

    private val viewBinding =
        ViewRecyclerHeaderBinding.inflate(LayoutInflater.from(context), this, false)
    private var onPullRefreshListener: OnPullRefreshListener? = null

    init {
        disableWhenHorizontalMove(true)
        isKeepHeaderWhenRefresh = true
        setRatioOfHeaderHeightToRefresh(1F)
        headerView = viewBinding.root
        addPtrUIHandler(this)
        setPtrHandler(
            object : PtrDefaultHandler() {
                override fun onRefreshBegin(layout: PtrFrameLayout) {
                    onPullRefreshListener?.onRefreshBegin()
                }
            })
    }

    override fun onUIReset(frame: PtrFrameLayout?) {
        v("onUIReset")
    }

    override fun onUIRefreshPrepare(frame: PtrFrameLayout?) {
        v("onUIRefreshPrepare")
        viewBinding.tvRefreshState.setText(R.string.refresh_pull_down_to_refresh)
    }

    override fun onUIRefreshBegin(frame: PtrFrameLayout?) {
        v("onUIRefreshBegin")
        viewBinding.tvRefreshState.setText(R.string.refresh_refreshing)
    }

    override fun onUIRefreshComplete(frame: PtrFrameLayout?) {
        v("onUIRefreshComplete")
        viewBinding.tvRefreshState.setText(R.string.refresh_refresh_complete)
    }

    override fun onUIPositionChange(layout: PtrFrameLayout, isUnderTouch: Boolean, status: Byte, ptrIndicator: PtrIndicator) {
        val offsetToRefresh: Int = layout.offsetToRefresh
        val currentPos = ptrIndicator.currentPosY
        val lastPos = ptrIndicator.lastPosY

        if (offsetToRefresh in (currentPos + 1)..lastPos) {
            if (isUnderTouch && status == PTR_STATUS_PREPARE) {
                viewBinding.tvRefreshState.setText(R.string.refresh_pull_down_to_refresh)
            }
        } else if (offsetToRefresh in lastPos until currentPos) {
            if (isUnderTouch && status == PTR_STATUS_PREPARE) {
                viewBinding.tvRefreshState.setText(R.string.refresh_release_to_refresh)
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    fun setOnPullRefreshListener(listener: OnPullRefreshListener) {
        this.onPullRefreshListener = listener
    }

    interface OnPullRefreshListener {
        fun onRefreshBegin()
    }
}