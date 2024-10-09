package com.tt.widget.view.fonttextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.StaticLayout
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.tt.widget.R

/**
 * Created by jarylan on 2017/4/13.
 * 自定义字体 TextView
 * xml 定义方式
 * <com.gdswww.wzlyapplication.ui.view.FontTextView app:font_type="regular"></com.gdswww.wzlyapplication.ui.view.FontTextView>
 * 默认用系统字体
 */
open class BaseFontTextView : AppCompatTextView {
    private var mLineY = 0
    private var mViewWidth = 0
    private var mText: StringBuffer? = null
    private var newText: StringBuffer? = null
    private var mPaint: Paint? = null

    /**
     * VIEW的高度
     */
    private var mHeight = 0

    /**
     * 显示的字数
     */
    private var numberOfWords = 0
    private var isFull = false

    constructor(context: Context?) : super(context!!)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    open fun initAttr(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.BaseFontTextView)
        isFull = a.getBoolean(R.styleable.BaseFontTextView_font_full, false)
        val underline = a.getBoolean(R.styleable.BaseFontTextView_font_underline, false)
        setUnderline(underline, false)
        val bold = a.getBoolean(R.styleable.BaseFontTextView_font_bold, false)
        setBoldText(bold, false)
        a.recycle()
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

    /**
     * 是否设置下划线
     */
    fun setUnderline(underline: Boolean) {
        setUnderline(underline, true)
    }

    private fun setUnderline(underline: Boolean, postInvalidate: Boolean) {
        paint.flags = if (underline) Paint.UNDERLINE_TEXT_FLAG else 0
        paint.isAntiAlias = true
        if (postInvalidate) {
            postInvalidate()
        }
    }

    override fun setWidth(width: Int) {
        if (this.layoutParams.width == width) {
            return
        }
        this.layoutParams.width = width
        requestLayout()
    }

    /**
     * 以下代码是为了解决TextView未满一行就换行的情况
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        if (isFull) {
            // 获取文本内容
            val text = text.toString()
            //以单例模式对文字进行拆分
            if (null == mText) {
                mText = StringBuffer(text)
                //获取画笔
                val paint = paint
                paint.isAntiAlias = true
                // 获取文字颜色将其设置到画笔上
                paint.color = currentTextColor
                //设置文字大小
                paint.textSize = textSize
                //设置字体，包括字体的类型，粗细，还有倾斜、颜色等
                paint.typeface = typeface
                paint.drawableState = drawableState
                //获取填写字数的宽
                mViewWidth = measuredWidth
                mPaint = paint
                //对文字进行分行处理
                caculateChangeLine()
            }
            //设置头部内边距
            mLineY = paddingTop
            mLineY += textSize.toInt()
            //避免出现空视图
            val layout = layout ?: return
            val fm = mPaint!!.fontMetrics
            var textHeight = Math.ceil((fm.descent - fm.ascent).toDouble()).toInt()
            textHeight = (textHeight * layout.spacingMultiplier + layout
                .spacingAdd).toInt() //获取文字的高度
            //将分割好滴文字进行排版
            val split = newText.toString().split("\n").toTypedArray()
            //此处设置文本显示的高度，适配一些手机无法显示
            if (split.size > 0) {
                //多设置了几行以避免显示不全(看情况进行修改)
                val i = split.size
                val setheight = textHeight * i
                //设置textview高度
                height = setheight
            }
            for (string in split) {
                //此处为源例子上的写法，标点符号换行问题还是存在（楼主引用，ToDBC(aaa)的方法进行了修改，已解决这个bug）
                val width = StaticLayout.getDesiredWidth(
                    string, 0,
                    string.length, paint
                )
                if (TextUtils.isEmpty(string)) {
                    continue
                }
                //验证是否足够一个屏幕的宽度
                val strWidth = mPaint!!.measureText(string + "好好").toInt()
                //判断是否足够一行显示的字数，足够久进行字的处理不够则直接画出来
                if (needScale(string) && string.trim { it <= ' ' }.length > numberOfWords - 5 && mViewWidth < strWidth) //，避免出现字数不够，字间距被画出来的字间距过大影响排版
                { // 判断是否结尾处需要换行，并且不是文本最后一行
                    drawScaledText(canvas, paddingLeft, string, width)
                } else {
                    // 将字符串直接画到控件上
                    canvas.drawText(string, paddingLeft.toFloat(), mLineY.toFloat(), mPaint!!)
                }
                mLineY += textHeight
            }
        } else {
            super.onDraw(canvas)
        }
    }

    /**
     * 计算出一行显示的文字
     */
    private fun caculateOneLine(str: String): String {
        //对一段没有\n的文字进行换行
        var returnStr: String
        val strWidth = mPaint!!.measureText(str).toInt()
        val len = str.length
        //大概知道分多少行
        val lineNum: Int = if (mViewWidth == 0) {
            0
        } else {
            strWidth / mViewWidth
        }
        var tempWidth: Int
        var lineStr: String
        val returnInt: Int
        if (lineNum == 0) {
            returnStr = str
            mHeight += LINE_HEIGHT
            return returnStr
        } else {
            //一行大概有多少个字
            var oneLine = len / (lineNum + 1)
            if (numberOfWords < oneLine) {
                numberOfWords = oneLine
            }
            lineStr = str.substring(0, oneLine)
            tempWidth = mPaint!!.measureText(lineStr).toInt()

            //如果小了 找到大的那个
            if (tempWidth < mViewWidth) {
                while (tempWidth < mViewWidth) {
                    oneLine++
                    lineStr = str.substring(0, oneLine)
                    tempWidth = mPaint!!.measureText(lineStr).toInt()
                }
                returnInt = oneLine - 1
                returnStr = lineStr.substring(0, lineStr.length - 2)
            } else  //大于宽找到小的
            {
                while (tempWidth > mViewWidth) {
                    oneLine--
                    lineStr = str.substring(0, oneLine)
                    tempWidth = mPaint!!.measureText(lineStr).toInt()
                }
                returnStr = lineStr.substring(0, lineStr.length - 1)
                returnInt = oneLine
            }
            mHeight += LINE_HEIGHT
            returnStr += """
                
                ${caculateOneLine(str.substring(returnInt - 1))}
                """.trimIndent()
        }
        return returnStr
    }

    fun caculateChangeLine() {
        newText = StringBuffer()
        val tempStr = mText.toString().split("\n").toTypedArray()
        for (aTempStr in tempStr) {
            val caculateOneLine = caculateOneLine(aTempStr)
            if (!TextUtils.isEmpty(caculateOneLine)) {
                newText!!.append(caculateOneLine)
                newText!!.append("\n")
            }
        }
        this.height = mHeight
    }

    private fun drawScaledText(
        canvas: Canvas, lineStart: Int, line: String,
        lineWidth: Float
    ) {
        var line = line
        var x = 0f
        // 判断是否是第一行
        if (isFirstLineOfParagraph(lineStart, line)) {
            canvas.drawText(TWO_CHINESE_BLANK, x, mLineY.toFloat(), paint)
            val bw = StaticLayout.getDesiredWidth(TWO_CHINESE_BLANK, paint)
            x += bw
            line = line.substring(3)
        }
        val gapCount = line.length - 1
        var i = 0
        if (line.length > 2 && line[0].toInt() == 12288 && line[1].toInt() == 12288) {
            val substring = line.substring(0, 2)
            val cw = StaticLayout.getDesiredWidth(substring, paint)
            canvas.drawText(substring, x, mLineY.toFloat(), paint)
            x += cw
            i += 2
        }
        val d = (mViewWidth - lineWidth) / gapCount
        while (i < line.length) {
            val c = line[i].toString()
            val cw = StaticLayout.getDesiredWidth(c, paint)
            canvas.drawText(c, x, mLineY.toFloat(), paint)
            x += cw + d
            i++
        }
    }

    private fun isFirstLineOfParagraph(lineStart: Int, line: String): Boolean {
        return line.length > 3 && line[0] == ' ' && line[1] == ' '
    }

    private fun needScale(line: String?): Boolean { // 判断是否需要换行
        return if (line == null || line.isEmpty()) {
            false
        } else {
            val charAt = line[line.length - 1]
            charAt != '\n'
        }
    }

    companion object {
        const val TWO_CHINESE_BLANK = "  "

        /**
         * 行高
         */
        private const val LINE_HEIGHT = 40
    }
}