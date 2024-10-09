package com.tt.dramatime.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import javax.annotation.Nullable


/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/7/8 下午2:39
 *   Desc : 监听滑动到底部的RecycleView
 * </pre>
 */
class SlideBottomMonitorRecycleView @JvmOverloads constructor(
    context: Context, @Nullable attrs: AttributeSet? = null, defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var mOnBottomCallback: OnBottomCallback? = null
    private var canScrollHorizontally = true

    interface OnBottomCallback {
        fun onBottom()
    }

    fun setOnBottomCallback(onBottomCallback: OnBottomCallback?) {
        this.mOnBottomCallback = onBottomCallback
    }

    override fun onScrolled(dx: Int, dy: Int) {}

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        when (state) {
            SCROLL_STATE_IDLE -> { // RecyclerView停止滚动
                if (!canScrollHorizontally(1) && canScrollHorizontally.not()) {
                    mOnBottomCallback?.onBottom()
                }
                canScrollHorizontally = canScrollHorizontally(1)
                Logger.d("RecyclerView:" + "停止滚动")
            }


            SCROLL_STATE_DRAGGING ->// RecyclerView正在被拖动
                Logger.d("RecyclerView:" + "正在拖动")

            SCROLL_STATE_SETTLING -> {// RecyclerView惯性滚动
                canScrollHorizontally = true
                Logger.d("RecyclerView:" + "惯性滑动")
            }
        }
    }

    /**
     * 其实就是它在起作用。
     */
    fun isSlideToBottom(): Boolean {
        return (this.computeVerticalScrollExtent() + this.computeVerticalScrollOffset() >= this.computeVerticalScrollRange())
    }
}