package com.cl.wanandroid.base.list.base

import com.cl.wanandroid.base.list.multitype.MultiTypeAdapter

open class BaseAdapter : MultiTypeAdapter() {

    /**
     * 设置列表数据 这个方法将会清空数据将传入的数据作为新的数据 并刷新列表
     * @param viewData 新的列表数据
     */
    open fun setViewData(viewData: List<BaseViewData<*>>) {
        items.clear()
        items.addAll(viewData)
        notifyDataSetChanged()
    }

    /**
     * 替换指定位置的列表数据 并刷新列表
     * @param viewData 新的数据
     * @param position 指定的替换位置
     */
    open fun replaceViewData(viewData: List<BaseViewData<*>>, position: Int) {
        if (position in 0 until itemCount) {
            items[position] = viewData[position]
            notifyItemChanged(position)
        }
    }

    /**
     * 向列表尾部添加数据 并刷新列表
     * @param viewData 新的数据
     */
    open fun addViewData(viewData: BaseViewData<*>) {
        val oldSize = itemCount
        items.add(viewData)
        notifyItemChanged(oldSize)
    }

    /**
     * 向列表尾部添加一个数据集 并刷新列表
     * @param viewData 新的数据集
     */
    open fun addViewData(viewData: List<BaseViewData<*>>) {
        val oldSize = itemCount
        items.addAll(viewData)
        notifyItemRangeInserted(oldSize, itemCount)
    }

    /**
     * 移除指定位置的数据 并刷新列表
     * @param position 指定的位置
     * @return 指定位置的数据(可能为空)
     */
    open fun removeViewData(position: Int): BaseViewData<*>? {
        var removedViewData: BaseViewData<*>? = null
        if (position in 0 until itemCount) {
            removedViewData = items.removeAt(position)
            notifyItemRemoved(position)
        }
        return removedViewData
    }

    /**
     * 在列表中移除指定的数据 并刷新列表
     * @param viewData 需要被移除的数据
     * @return 指定的数据(可能为空)
     */
    open fun removeViewData(viewData: BaseViewData<*>): BaseViewData<*>? {
        val position = items.indexOf(viewData)
        return removeViewData(position)
    }

    /**
     * 清空数据 并刷新列表
     */
    open fun clearViewData() {
        val oldSize = itemCount
        items.clear()
        notifyItemRangeRemoved(0, oldSize)
    }

    /**
     * 获取指定列表指定位置数据(可能为空)
     */
    open fun getViewData(position: Int): BaseViewData<*>? {
        return if (position in 0 until itemCount) {
            items[position]
        } else {
            return null
        }
    }
}