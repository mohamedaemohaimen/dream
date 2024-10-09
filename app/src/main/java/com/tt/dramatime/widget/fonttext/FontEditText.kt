package com.tt.dramatime.widget.fonttext

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.tt.dramatime.R
import com.tt.dramatime.manager.LanguageManager

/**
 * 自定义字体 EditText
 * xml 定义方式
 * 默认用系统字体
 */
class FontEditText@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {


    init {
        textDirection =
            if (LanguageManager.isArabicLocale(context)) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR

        val a = context.obtainStyledAttributes(attrs, R.styleable.FontEditText)
        val bold = a.getBoolean(R.styleable.FontEditText_font_edit_bold, false)

        setBoldText(bold, false)

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
}