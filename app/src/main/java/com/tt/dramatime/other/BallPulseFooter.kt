package com.tt.dramatime.other

import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.simple.SimpleComponent
import com.scwang.smart.refresh.layout.util.SmartUtil
import com.tt.dramatime.R
import kotlin.math.min


/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/5/14 10:35
 *   Desc : 球脉冲底部加载组件
 * </pre>
 */
class BallPulseFooter @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SimpleComponent(context, attrs, 0), RefreshFooter {
    //<editor-fold desc="属性变量">
    private var mManualNormalColor: Boolean = false
    private var mManualAnimationColor: Boolean = false

    private var mPaint: Paint

    private var mNormalColor: Int = -0x111112
    private var mAnimatingColor: Int = -0x18a6ba

    private var mCircleSpacing: Float


    private var mStartTime: Long = 0
    private var mIsStarted: Boolean = false
    private var mInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator()

    //</editor-fold>
    //<editor-fold desc="构造方法">
    init {
        val thisView: View = this
        thisView.minimumHeight = SmartUtil.dp2px(60f)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.BallPulseFooter)

        mPaint = Paint()
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true

        mSpinnerStyle = SpinnerStyle.Translate
        mSpinnerStyle = SpinnerStyle.values[ta.getInt(
            R.styleable.BallPulseFooter_srlClassicsSpinnerStyle, mSpinnerStyle.ordinal
        )]

        if (ta.hasValue(R.styleable.BallPulseFooter_srlNormalColor)) {
            setNormalColor(ta.getColor(R.styleable.BallPulseFooter_srlNormalColor, 0))
        }
        if (ta.hasValue(R.styleable.BallPulseFooter_srlAnimatingColor)) {
            setAnimatingColor(ta.getColor(R.styleable.BallPulseFooter_srlAnimatingColor, 0))
        }

        ta.recycle()

        mCircleSpacing = SmartUtil.dp2px(4f).toFloat()
    }

    //</editor-fold>
    override fun dispatchDraw(canvas: Canvas) {
        val thisView: View = this
        val width = thisView.width
        val height = thisView.height
        val radius = ((min(width.toDouble(), height.toDouble()) - mCircleSpacing * 2) / 6).toFloat()
        val x = width / 2f - (radius * 2 + mCircleSpacing)
        val y = height / 2f

        val now = System.currentTimeMillis()

        for (i in 0..2) {
            val time = now - mStartTime - 120 * (i + 1)
            var percent = if (time > 0) ((time % 750) / 750f) else 0f
            percent = mInterpolator.getInterpolation(percent)

            canvas.save()

            val translateX = x + radius * 2 * i + mCircleSpacing * i
            canvas.translate(translateX, y)

            if (percent < 0.5) {
                val scale = 1 - percent * 2 * 0.7f
                canvas.scale(scale, scale)
            } else {
                val scale = percent * 2 * 0.7f - 0.4f
                canvas.scale(scale, scale)
            }

            canvas.drawCircle(0f, 0f, radius, mPaint)
            canvas.restore()
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
        mPaint.color = mAnimatingColor
    }

    override fun onFinish(layout: RefreshLayout, success: Boolean): Int {
        mIsStarted = false
        mStartTime = 0
        mPaint.color = mNormalColor
        return 0
    }

    /**
     * @param colors 对应Xml中配置的 srlPrimaryColor srlAccentColor
     */
    @Deprecated(
        """只由框架调用
      使用者使用 {@link RefreshLayout#setPrimaryColorsId(int...)}"""
    )
    override fun setPrimaryColors(@ColorInt vararg colors: Int) {
        if (!mManualAnimationColor && colors.size > 1) {
            setAnimatingColor(colors[0])
            mManualAnimationColor = false
        }
        if (!mManualNormalColor) {
            if (colors.size > 1) {
                setNormalColor(colors[1])
            } else if (colors.size > 0) {
                setNormalColor(ColorUtils.compositeColors(-0x66000001, colors[0]))
            }
            mManualNormalColor = false
        }
    }

    //</editor-fold>
    //<editor-fold desc="开放接口 - API">
    fun setSpinnerStyle(mSpinnerStyle: SpinnerStyle?): BallPulseFooter {
        this.mSpinnerStyle = mSpinnerStyle
        return this
    }

    fun setNormalColor(@ColorInt color: Int): BallPulseFooter {
        mNormalColor = color
        mManualNormalColor = true
        if (!mIsStarted) {
            mPaint.color = color
        }
        return this
    }

    fun setAnimatingColor(@ColorInt color: Int): BallPulseFooter {
        mAnimatingColor = color
        mManualAnimationColor = true
        if (mIsStarted) {
            mPaint.color = color
        }
        return this
    } //</editor-fold>
}