package com.tt.dramatime.widget.fonttext

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import com.hjq.shape.view.ShapeTextView
import com.tt.dramatime.R

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 可自定义背景
 * </pre>
 */
open class MyShapeTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ShapeTextView(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MyShapeTextView)
        val fontType = a.getInteger(R.styleable.MyShapeTextView_stv_font_type, 0)
        val useStrikeThruText =
            a.getBoolean(R.styleable.MyShapeTextView_stv_strike_thru_text, false)
        val bold = a.getBoolean(R.styleable.MyShapeTextView_stv_font_bold, false)
        if (useStrikeThruText) {
            paint.flags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
        }
        setTypefaceType(fontType)
        setBoldText(bold, false)
        a.recycle()
    }


    /**
     * 设置字体
     *
     * @param type 字体类型 [WzlyFontFactory]
     */
    fun setTypefaceType(type: Int) {
        var mtf: Typeface? = null
        when (type) {
            FontFactory.DIN_BOLD -> mtf = FontFactory(context).mDinBold
            FontFactory.DIN_MEDIUM -> mtf = FontFactory(context).mDinMedium
            FontFactory.DIN_REGULAR -> mtf = FontFactory(context).mDinRegular
            else -> {
            }
        }
        if (mtf == null) {
            return
        }
        typeface = mtf
    }

    /**
     * 设置字体是否为粗体
     *
     * @param bold true 为粗
     */
    fun setBoldText(bold: Boolean) {
        setBoldText(bold, true)
    }

    private fun setBoldText(bold: Boolean, postInvalidate: Boolean) {
        val tp = paint
        tp.isFakeBoldText = bold
        tp.isAntiAlias = true
        if (postInvalidate) {
            postInvalidate()
        }
    }
}