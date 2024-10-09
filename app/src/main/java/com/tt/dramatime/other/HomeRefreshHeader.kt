package com.tt.dramatime.other

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.airbnb.lottie.LottieAnimationView
import com.blankj.utilcode.util.ConvertUtils
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.simple.SimpleComponent
import com.tt.dramatime.R

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 自定义顶部刷新动画
 *          参考 https://github.com/scwang90/SmartRefreshLayout/blob/master/art/md_custom.md
 * </pre>
 */
class HomeRefreshHeader @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SimpleComponent(context, attrs, 0), RefreshHeader {

    private var lottieView: LottieAnimationView
    private var refreshState: RefreshState? = null
    private var finished: Boolean = false

    init {
        mSpinnerStyle = SpinnerStyle.FixedBehind
        lottieView = LottieAnimationView(context)
        lottieView.setAnimation(R.raw.head_refresh)
        val layoutParams = LayoutParams(ConvertUtils.dp2px(51f), ConvertUtils.dp2px(51f))
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        layoutParams.bottomMargin = ConvertUtils.dp2px(20f)
        lottieView.layoutParams = layoutParams
        lottieView.repeatCount = Int.MAX_VALUE
        addView(lottieView)
    }

    /**
     * 尺寸定义初始化完成 （如果高度不改变（代码修改：setHeader），只调用一次, 在RefreshLayout#onMeasure中调用）
     * @param kernel RefreshKernel 核心接口（用于完成高级Header功能）
     * @param height HeaderHeight or FooterHeight
     * @param maxDragHeight 最大拖动高度
     */
    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {}

    /**
     * 手指释放之后的持续动画（会连续多次调用，用于实时控制动画关键帧）
     * @param height Header的高度
     * @param maxDragHeight 最大拖动高度
     */
    override fun onReleased(layout: RefreshLayout, height: Int, maxDragHeight: Int) {}

    override fun onMoving(
        dragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int
    ) {
        if (refreshState == RefreshState.Refreshing) {
            return
        }
        if (dragging || (!lottieView.isAnimating && !finished)) {
            if (refreshState != RefreshState.Refreshing && lottieView.isAnimating.not()) {
                lottieView.playAnimation()
            }
        }
    }

    override fun onStateChanged(
        refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState
    ) {
        refreshState = newState
        if (newState == RefreshState.PullDownToRefresh) {
            finished = false
        }
    }

    override fun onFinish(layout: RefreshLayout, success: Boolean): Int {
        lottieView.cancelAnimation()
        finished = true
        return 400
    }

}