package com.tt.dramatime.widget.fonttext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import com.tt.dramatime.R
import com.tt.widget.view.fonttextview.BaseFontTextView


/**
 * 自定义字体 TextView
 * xml 定义方式
 * 默认用系统字体
 */
class RemovePaddingTextView : BaseFontTextView {

    constructor(context: Context?) : this(context, null, 0)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs,defStyleAttr
    ) {
        initAttr(context, attrs)
    }

    private var mRemoveFontPadding = false //是否去除字体内边距，true：去除 false：不去除

    private val mPaint: Paint = paint
    private val mBounds = Rect()


    override fun initAttr(context: Context, attrs: AttributeSet?) {
        super.initAttr(context, attrs)
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.RemovePaddingTextView)
        mRemoveFontPadding = a.getBoolean(R.styleable.RemovePaddingTextView_remove_default_padding, false);
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mRemoveFontPadding) {
            calculateTextParams()
            setMeasuredDimension(mBounds.right - mBounds.left, -mBounds.top + mBounds.bottom)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawText(canvas)
    }

    /**
     * 计算文本参数
     */
    private fun calculateTextParams(): String {
        val text = text.toString()
        val textLength = text.length
        mPaint.getTextBounds(text, 0, textLength, mBounds)
        if (textLength == 0) {
            mBounds.right = mBounds.left
        }
        return text
    }

    /**
     * 绘制文本
     */
    private fun drawText(canvas: Canvas) {
        val text = calculateTextParams()
        val left: Int = mBounds.left
        val bottom: Int = mBounds.bottom
        mBounds.offset(-mBounds.left, -mBounds.top)
        mPaint.isAntiAlias = true
        mPaint.color = currentTextColor
        canvas.drawText(text, (-left).toFloat(), (mBounds.bottom - bottom).toFloat(), mPaint)
    }

}