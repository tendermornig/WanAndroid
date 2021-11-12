package com.cl.wanandroid.item

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cl.wanandroid.R
import com.cl.wanandroid.base.list.base.BaseItemViewDelegate
import com.cl.wanandroid.constant.LoadMoreState
import com.cl.wanandroid.databinding.ViewRecyclerFooterBinding

class LoadMoreViewDelegate :
    BaseItemViewDelegate<LoadMoreViewData, LoadMoreViewDelegate.ViewHolder>() {

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        context: Context,
        parent: ViewGroup
    ): ViewHolder {
        return ViewHolder(ViewRecyclerFooterBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: LoadMoreViewData) {
        super.onBindViewHolder(holder, item)
        holder.viewBinding.run {
            when (item.value) {
                LoadMoreState.GONE -> {
                    footerRoot.isVisible = false
                }
                LoadMoreState.LOADING -> {
                    footerRoot.isVisible = true
                    tvLoadMoreState.setText(R.string.load_more_loading)
                }
                LoadMoreState.ERROR -> {
                    footerRoot.isVisible = true
                    tvLoadMoreState.setText(R.string.load_more_network_error)
                }
                LoadMoreState.NO_NETWORK -> {
                    footerRoot.isVisible = true
                    tvLoadMoreState.setText(R.string.load_more_no_network)
                }
                LoadMoreState.NO_MORE -> {
                    footerRoot.isVisible = true
                    tvLoadMoreState.setText(R.string.load_more_no_more)
                }
            }
        }
    }

    /**
     * 如果是StaggeredGridLayoutManager，加载更多应该展示成通栏样式
     */
    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }
    }

    class ViewHolder(val viewBinding: ViewRecyclerFooterBinding) :
        RecyclerView.ViewHolder(viewBinding.root)
}