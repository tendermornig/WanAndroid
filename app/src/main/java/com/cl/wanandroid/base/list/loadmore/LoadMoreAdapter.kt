package com.cl.wanandroid.base.list.loadmore

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cl.wanandroid.base.list.base.BaseAdapter
import com.cl.wanandroid.base.list.base.BaseViewData
import com.cl.wanandroid.constant.LoadMoreState
import com.cl.wanandroid.item.LoadMoreViewData


class LoadMoreAdapter : BaseAdapter() {

    private val loadMoreViewData = LoadMoreViewData(LoadMoreState.LOADING)

    /**
     * 重写setViewData，添加加载更多条目
     */
    override fun setViewData(viewData: List<BaseViewData<*>>) {
        val mutableViewData = viewData.toMutableList()
        mutableViewData.add(loadMoreViewData)
        super.setViewData(viewData)
    }

    /**
     * 重写replaceViewData 将替换条目的位置-1 这样才是正确的替换位置
     */
    override fun replaceViewData(viewData: List<BaseViewData<*>>, position: Int) {
        if (position in 0 until itemCount - 1) {
            items[position] = viewData[position]
            notifyItemChanged(position)
        }
    }

    /**
     * 重写addViewData 将替换条目的位置-1 道理相同
     */
    override fun addViewData(viewData: BaseViewData<*>) {
        val oldSize = itemCount - 1
        items.add(viewData)
        notifyItemChanged(oldSize)
    }

    /**
     * 重写addViewData 将替换条目的位置-1 道理相同
     */
    override fun addViewData(viewData: List<BaseViewData<*>>) {
        val oldSize = itemCount
        items.addAll(viewData)
        notifyItemRangeInserted(oldSize, itemCount)
    }

    override fun removeViewData(position: Int): BaseViewData<*>? {
        var removedViewData: BaseViewData<*>? = null
        if (position in 0 until itemCount - 1) {
            removedViewData = items.removeAt(position)
            notifyItemRemoved(position)
        }
        return removedViewData
    }

    fun isLoadMoreViewData(position: Int): Boolean {
        return position == itemCount - 1 && items[position] is LoadMoreViewData
    }

    fun setLoadMoreState(@LoadMoreState loadMoreState: Int) {
        val position = itemCount - 1
        if (isLoadMoreViewData(position)) {
            loadMoreViewData.value = loadMoreState
            notifyItemChanged(position)
        }
    }

    @LoadMoreState
    fun getLoadMoreState(): Int {
        return loadMoreViewData.value
    }

    /**
     * 如果是GridLayoutManager，加载更多应该展示成通栏样式
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val lm = recyclerView.layoutManager
        if (lm is GridLayoutManager) {
            val oldSpanSizeLockup = lm.spanSizeLookup
            lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val viewData = getViewData(position)
                    return if (viewData is LoadMoreViewData) {
                        lm.spanCount
                    } else {
                        oldSpanSizeLockup.getSpanSize(position)
                    }
                }
            }
        }
    }
}