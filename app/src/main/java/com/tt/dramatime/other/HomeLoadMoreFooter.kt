package com.tt.dramatime.other

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.simple.SimpleComponent
import com.scwang.smart.refresh.layout.util.SmartUtil
import com.tt.dramatime.R


/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/5/14 10:35
 *   Desc : 球脉冲底部加载组件
 * </pre>
 */
class HomeLoadMoreFooter @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SimpleComponent(context, attrs, 0), RefreshFooter {
    private var mStartTime: Long = 0
    private var mIsStarted: Boolean = false
    private var noMoreData: Boolean = false

    init {
        val thisView: View = this
        thisView.minimumHeight = SmartUtil.dp2px(60f)
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        val view = View.inflate(context, R.layout.home_view_load_more, null)
        view.layoutParams = layoutParams
        addView(view)
    }

    //</editor-fold>
    override fun dispatchDraw(canvas: Canvas) {
        val thisView: View = this
        if (noMoreData) {
            findViewById<LinearLayout>(R.id.load_more_loading_view).visibility = View.GONE
            findViewById<FrameLayout>(R.id.load_more_load_end_view).visibility = View.VISIBLE
        }else{
            findViewById<LinearLayout>(R.id.load_more_loading_view).visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.load_more_load_end_view).visibility = View.GONE
        }

        super.dispatchDraw(canvas)

        if (mIsStarted) {
            thisView.invalidate()
        }
    }


    //<editor-fold desc="刷新方法 - RefreshFooter">
    override fun onStartAnimator(layout: RefreshLayout, height: Int, maxDragHeight: Int) {
        if (mIsStarted) return
        val thisView: View = this
        thisView.invalidate()
        mIsStarted = true
        mStartTime = System.currentTimeMillis()

    }

    override fun onFinish(layout: RefreshLayout, success: Boolean): Int {
        mIsStarted = false
        mStartTime = 0
        return 0
    }



    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        this.noMoreData = noMoreData
        return true
    }
}