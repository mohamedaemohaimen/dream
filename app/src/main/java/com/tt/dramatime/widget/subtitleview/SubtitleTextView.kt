package com.tt.dramatime.widget.subtitleview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView


/**
 * 显示字幕的字体样式
 */
class SubtitleTextView : AppCompatTextView, OnTouchListener {

    private var context: Context? = null

    private var listener: SubtitleClickListener? = null

    private var strokePaint: TextPaint? = null
    private var borderText: TextView? = null ///用于描边的TextView

    constructor(context: Context) : super(context) {
        borderText = TextView(context)
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        borderText = TextView(context, attrs)
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        borderText = TextView(context, attrs, defStyleAttr)
        init(context)
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        borderText?.setLayoutParams(params)
        super.setLayoutParams(params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val tt = borderText?.text

        //两个TextView上的文字必须一致
        if (tt == null || tt != this.text) {
            borderText?.text = text
            this.postInvalidate()
        }
        borderText?.measure(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        borderText?.layout(left, top, right, bottom)
    }


    private fun init(context: Context) {
        this.context = context
        strokePaint = borderText?.paint
        strokePaint?.strokeWidth = 4f //设置描边宽度
        strokePaint?.color = Color.BLACK
        strokePaint?.style = Paint.Style.STROKE //对文字只描边
        borderText?.setGravity(gravity)
        this.setOnTouchListener(this)
    }

    fun setStrokeColor(@ColorInt color: Int, size: Float) {
        borderText?.setTextColor(color)
        borderText?.textSize = size
        invalidate()
    }

    fun setStrokeWidth(width: Int) {
        strokePaint?.strokeWidth = width.toFloat()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        borderText?.draw(canvas)
        super.onDraw(canvas)
    }

    fun setSubtitleOnTouchListener(listener: SubtitleClickListener?) {
        this.listener = listener
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (listener != null) listener!!.onClickDown()
            MotionEvent.ACTION_UP -> if (listener != null) listener!!.onClickUp()
        }
        return true
    }

}

