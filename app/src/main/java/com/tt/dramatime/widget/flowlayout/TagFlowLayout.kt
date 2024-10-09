package com.tt.dramatime.widget.flowlayout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tt.dramatime.R
import com.tt.dramatime.widget.flowlayout.TagAdapter.OnDataChangedListener
import timber.log.Timber

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 标签流式布局
 * </pre>
 */
class TagFlowLayout<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FlowLayout(context, attrs, defStyle), OnDataChangedListener {

    companion object {
        private const val TAG = "TagFlowLayout"
        private const val KEY_CHOOSE_POS = "key_choose_pos"
        private const val KEY_DEFAULT = "key_default"
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }


    private var mTagAdapter: TagAdapter<T>? = null
    private var mAutoSelectEffect = true
    private var mRequired = false
    private var mDefaultSelection = false

    /**
     * -1为不限制数量
     */
    private var mSelectedMax = -1
    private var mMotionEvent: MotionEvent? = null
    private val mSelectedView: MutableSet<Int> = HashSet()

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout)
        mAutoSelectEffect = ta.getBoolean(R.styleable.TagFlowLayout_auto_select_effect, true)
        mRequired = ta.getBoolean(R.styleable.TagFlowLayout_required, false)
        mDefaultSelection = ta.getBoolean(R.styleable.TagFlowLayout_default_selection, false)
        mSelectedMax = ta.getInt(R.styleable.TagFlowLayout_max_select, -1)
        ta.recycle()
        if (mAutoSelectEffect) {
            isClickable = true
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(KEY_DEFAULT, super.onSaveInstanceState())
        var selectPos = ""
        if (mSelectedView.size > 0) {
            for (key in mSelectedView) {
                selectPos += "$key|"
            }
            selectPos = selectPos.substring(0, selectPos.length - 1)
        }
        bundle.putString(KEY_CHOOSE_POS, selectPos)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            val mSelectPos = bundle.getString(KEY_CHOOSE_POS)
            if (!TextUtils.isEmpty(mSelectPos)) {
                val split = mSelectPos!!.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                for (pos in split) {
                    val index = pos.toInt()
                    mSelectedView.add(index)
                    val tagView = getChildAt(index) as TagView?
                    if (tagView != null) {
                        tagView.isChecked = true
                    }
                }
            }
            super.onRestoreInstanceState(bundle.getParcelable(KEY_DEFAULT))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cCount = childCount
        for (i in 0 until cCount) {
            val tagView = getChildAt(i) as TagView
            //开启默认选中后 默认选中第一个
            if (mDefaultSelection && i == 0) {
                doSelect(tagView, 0)
            }
            if (tagView.visibility == GONE) {
                continue
            }
            if (tagView.tagView.visibility == GONE) {
                tagView.visibility = GONE
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    interface OnSelectListener {
        fun onSelected(selectPosSet: Set<Int>?)
    }

    private var mOnSelectListener: OnSelectListener? = null
    fun setOnSelectListener(onSelectListener: OnSelectListener?) {
        mOnSelectListener = onSelectListener
        if (mOnSelectListener != null) {
            isClickable = true
        }
    }

    interface OnTagClickListener {
        fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean
    }

    private var mOnTagClickListener: OnTagClickListener? = null
    fun setOnTagClickListener(onTagClickListener: OnTagClickListener?) {
        mOnTagClickListener = onTagClickListener
        if (onTagClickListener != null) {
            isClickable = true
        }
    }

    private fun changeAdapter() {
        removeAllViews()
        val adapter = mTagAdapter
        var tagViewContainer: TagView?
        val preCheckedList: MutableSet<Int> = mTagAdapter!!.preCheckedList
        for (i in 0 until adapter!!.count) {
            val tagView = adapter.getView(this, i, adapter.getItem(i))
            tagViewContainer = TagView(context)
            tagView!!.isDuplicateParentStateEnabled = true
            if (tagView.layoutParams != null) {
                tagViewContainer.layoutParams = tagView.layoutParams
            } else {
                val lp = MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                lp.setMargins(
                    dip2px(context, 5f), dip2px(
                        context, 5f
                    ), dip2px(context, 5f), dip2px(
                        context, 5f
                    )
                )
                tagViewContainer.layoutParams = lp
            }
            tagViewContainer.addView(tagView)
            addView(tagViewContainer)
            if (preCheckedList.contains(i)) {
                tagViewContainer.isChecked = true
            }
            if (mTagAdapter!!.setSelected(i, adapter.getItem(i))) {
                mSelectedView.add(i)
                tagViewContainer.isChecked = true
            }
        }
        mSelectedView.addAll(preCheckedList)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            mMotionEvent = MotionEvent.obtain(event)
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        if (mMotionEvent == null) {
            return super.performClick()
        }
        val x = mMotionEvent!!.x.toInt()
        val y = mMotionEvent!!.y.toInt()
        mMotionEvent = null
        val child = findChild(x, y)
        val pos = findPosByView(child)
        if (child != null) {
            doSelect(child, pos)
            if (mOnTagClickListener != null) {
                return mOnTagClickListener!!.onTagClick(child.tagView, pos, this)
            }
        }
        return true
    }

    fun setMaxSelectCount(count: Int) {
        if (mSelectedView.size > count) {
            Timber.tag(TAG)
                .w("you has already select more than $count views , so it will be clear .")
            mSelectedView.clear()
        }
        mSelectedMax = count
    }

    val selectedList: Set<Int>
        get() = HashSet(mSelectedView)

    private fun doSelect(child: TagView, position: Int) {
        if (mAutoSelectEffect) {
            if (!child.isChecked) {
                //处理max_select=1的情况
                if (mSelectedMax == 1 && mSelectedView.size == 1) {
                    val iterator: Iterator<Int> = mSelectedView.iterator()
                    val preIndex = iterator.next()
                    val pre = getChildAt(preIndex) as TagView
                    pre.isChecked = false
                    child.isChecked = true
                    mSelectedView.remove(preIndex)
                    mSelectedView.add(position)
                } else {
                    if (mSelectedMax > 0 && mSelectedView.size >= mSelectedMax) {
                        return
                    }
                    child.isChecked = true
                    mSelectedView.add(position)
                }
            } else {
                if (mRequired.not() || mSelectedMax != 1) {
                    child.isChecked = false
                    mSelectedView.remove(position)
                }
            }
            if (mOnSelectListener != null) {
                mOnSelectListener!!.onSelected(HashSet(mSelectedView))
            }
        }
    }

    var adapter: TagAdapter<T>?
        get() = mTagAdapter
        set(adapter) {
            mTagAdapter = adapter
            mTagAdapter!!.setOnDataChangedListener(this)
            mSelectedView.clear()
            changeAdapter()
        }


    private fun findPosByView(child: View?): Int {
        val cCount = childCount
        for (i in 0 until cCount) {
            val v = getChildAt(i)
            if (v === child) {
                return i
            }
        }
        return -1
    }

    private fun findChild(x: Int, y: Int): TagView? {
        val cCount = childCount
        for (i in 0 until cCount) {
            val v = getChildAt(i) as TagView
            if (v.visibility == GONE) {
                continue
            }
            val outRect = Rect()
            v.getHitRect(outRect)
            if (outRect.contains(x, y)) {
                return v
            }
        }
        return null
    }

    override fun onChanged() {
        mSelectedView.clear()
        changeAdapter()
    }

}
