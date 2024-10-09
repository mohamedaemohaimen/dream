package com.tt.dramatime.widget.flowlayout

import android.view.View

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 标签适配器
 * </pre>
 */
abstract class TagAdapter<T> {
    private var mTagDatas: List<T>?
    private var mOnDataChangedListener: OnDataChangedListener? = null
    val preCheckedList = HashSet<Int>()

    constructor(datas: List<T>?) {
        mTagDatas = datas
    }

    constructor(datas: Array<T>) {
        mTagDatas = ArrayList(listOf(*datas))
    }

    interface OnDataChangedListener {
        fun onChanged()
    }

    fun setOnDataChangedListener(listener: OnDataChangedListener?) {
        mOnDataChangedListener = listener
    }

    fun setSelectedList(vararg poses: Int) {
        val set: MutableSet<Int> = HashSet()
        for (pos in poses) {
            set.add(pos)
        }
        setSelectedList(set)
    }

    fun setSelectedList(set: Set<Int>?) {
        preCheckedList.clear()
        if (set != null) preCheckedList.addAll(set)
        notifyDataChanged()
    }

    val count: Int
        get() = if (mTagDatas == null) 0 else mTagDatas!!.size

    fun notifyDataChanged() {
        if (mOnDataChangedListener != null) mOnDataChangedListener!!.onChanged()
    }

    fun getItem(position: Int): T {
        return mTagDatas!![position]
    }

    abstract fun getView(parent: FlowLayout?, position: Int, bean: T?): View?

    fun setSelected(position: Int, bean: T?): Boolean {
        return false
    }
}
