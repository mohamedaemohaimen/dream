package com.tt.widget.view.fonttextview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.tt.widget.R

/**
 * @author jarylan on 2017/4/13.
 * 自定义字体 TextView
 * xml 定义方式
 * <com.gdswww.wzlyapplication.ui.view.FontTextView app:font_type="regular"></com.gdswww.wzlyapplication.ui.view.FontTextView>
 * 默认用系统字体
 */
open class BaseFontEditView : AppCompatEditText {

    constructor(context: Context?) : super(context!!)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttr(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initAttr(context, attrs)
    }

    open fun initAttr(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.BaseFontTextView)
        val bold = a.getBoolean(R.styleable.BaseFontTextView_font_bold, false)
        setBoldText(bold)
        a.recycle()
    }

    /**
     * 设置字体是否为粗体
     *
     * @param bold true 为粗
     */
    fun setBoldText(bold: Boolean) {
        val tp = paint
        tp.isFakeBoldText = bold
    }

    override fun setWidth(width: Int) {
        if (this.layoutParams.width == width) {
            return
        }
        this.layoutParams.width = width
        requestLayout()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}