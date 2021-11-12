package com.cl.wanandroid.base.list.loadmore

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import kotlin.math.abs


class LoadMoreRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private lateinit var scrollChangeListener: LoadMoreRecyclerScrollListener
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var canLoadMore = true

    override fun setAdapter(adapter: Adapter<*>?) {
        scrollChangeListener =
            object : LoadMoreRecyclerScrollListener(layoutManager ?: LinearLayoutManager(context)) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    // 触发预加载
                    if (canLoadMore) {
                        onLoadMoreListener?.onLoadMore(page, totalItemsCount)
                    }
                }
            }
        addOnScrollListener(scrollChangeListener)
        super.setAdapter(adapter)
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        this.onLoadMoreListener = listener
    }

    fun setCanLoadMore(canLoadMore: Boolean) {
        this.canLoadMore = canLoadMore
    }

    /**
     * 重置LoadMoreRecyclerScrollListener，防止不触发预加载
     */
    fun resetLoadMoreListener() {
        scrollChangeListener.reset()
    }

    interface OnLoadMoreListener {
        fun onLoadMore(page: Int, totalItemsCount: Int)
    }
}