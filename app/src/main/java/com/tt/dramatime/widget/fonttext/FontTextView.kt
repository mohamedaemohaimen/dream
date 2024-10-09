package com.tt.dramatime.widget.fonttext

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.tt.dramatime.R
import com.tt.dramatime.manager.LanguageManager

/**
 * 自定义字体 TextView
 * xml 定义方式
 * <com.tt.widget.fonttext.FontTextView app:font_type="regular">
 * 默认用系统字体
 */
class FontTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        textDirection =
            if (LanguageManager.isArabicLocale(context)) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR

        val a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView)
        val fontType = a.getInteger(R.styleable.FontTextView_font_type, 0)
        val useStrikeThruText = a.getBoolean(R.styleable.FontTextView_use_strike_thru_text, false)
        if (useStrikeThruText) {
            paint.flags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
        }
        val bold = a.getBoolean(R.styleable.FontTextView_font_bold, false)

        if (a.hasValue(R.styleable.FontTextView_font_bold_pro)) {
            setTypeface(null, Typeface.BOLD)
        }
        setBoldText(bold, false)

        setTypefaceType(fontType)
        a.recycle()
    }

    fun setBoldText(bold: Boolean, postInvalidate: Boolean) {
        val tp = paint
        tp.isFakeBoldText = bold
        tp.isAntiAlias = true
        if (postInvalidate) {
            postInvalidate()
        }
    }

    /**
     * 设置字体
     *
     * @param type 字体类型[FontFactory]
     */
    fun setTypefaceType(type: Int) {
        var mtf: Typeface? = null
        when (type) {
            FontFactory.DIN_BOLD -> mtf = FontFactory(context).mDinBold
            FontFactory.DIN_MEDIUM -> mtf = FontFactory(context).mDinMedium
            FontFactory.DIN_REGULAR -> mtf = FontFactory(context).mDinRegular
            FontFactory.SF_PRO_HEAVY -> mtf = FontFactory(context).mHeavyItalic
            else -> {
            }
        }
        if (mtf == null) {
            return
        }
        typeface = mtf
    }
}