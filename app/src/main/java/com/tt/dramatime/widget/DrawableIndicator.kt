package com.tt.dramatime.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import com.tt.dramatime.R
import com.tt.dramatime.manager.LanguageManager
import com.youth.banner.indicator.BaseIndicator

class DrawableIndicator : BaseIndicator {
    private var normalBitmap: Bitmap? = null
    private var selectedBitmap: Bitmap? = null

    /**
     * 实例化Drawable指示器 ，也可以通过自定义属性设置
     *
     * @param context
     * @param normalResId
     * @param selectedResId
     */
    constructor(
        context: Context?, @DrawableRes normalResId: Int, @DrawableRes selectedResId: Int
    ) : super(context) {
        normalBitmap = BitmapFactory.decodeResource(resources, normalResId)
        selectedBitmap = BitmapFactory.decodeResource(resources, selectedResId)
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context, attrs, defStyleAttr
    ) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DrawableIndicator)
        val normal = a.getDrawable(R.styleable.DrawableIndicator_normal_drawable) as BitmapDrawable?
        val selected =
            a.getDrawable(R.styleable.DrawableIndicator_selected_drawable) as BitmapDrawable?
        normalBitmap = normal!!.bitmap
        selectedBitmap = selected!!.bitmap
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val count = config.indicatorSize
        if (count <= 1) {
            return
        }
        setMeasuredDimension(
            normalBitmap!!.width * (count - 1) + selectedBitmap!!.width + config.indicatorSpace * (count - 1),
            Math.max(normalBitmap!!.height, selectedBitmap!!.height)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val count = config.indicatorSize
        if (count <= 1 || normalBitmap == null || selectedBitmap == null) {
            return
        }

        if (LanguageManager.isArabicLocale(context)) {
            var right = width.toFloat()

            val indicatorSpace = config.indicatorSpace.toFloat()

            for (i in 0 until count) {
                val indicatorWidth = (if (config.currentPosition == i) selectedBitmap!!.width else normalBitmap!!.width)
                canvas.drawBitmap(
                    (if (config.currentPosition == i) selectedBitmap else normalBitmap)!!,
                    right - indicatorWidth,
                    0f,
                    mPaint
                )
                right -= indicatorWidth + indicatorSpace
            }
        }else{
            var left = 0f
            for (i in 0 until count) {
                canvas.drawBitmap(
                    (if (config.currentPosition == i) selectedBitmap else normalBitmap)!!,
                    left,
                    0f,
                    mPaint
                )
                left += ((if (config.currentPosition == i) selectedBitmap!!.width else normalBitmap!!.width) + config.indicatorSpace).toFloat()
            }

        }
    }
}