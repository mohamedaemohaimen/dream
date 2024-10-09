package com.tt.dramatime.util

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/8/19 下午3:28
 *   Desc : 字符串工具类
 * </pre>
 */
object SpannableStringUtils {

    fun highlightFirstKeyword(textView: TextView, text: String?, keyword: String?, color: Int) {
        // 创建一个 SpannableString
        val spannableString = SpannableString(text)

        if (text == null || keyword == null) {
            textView.text = text
            return
        }

        // 将输入的文本和关键字都转换为小写，以便不区分大小写进行比较
        val lowerCaseText = text.lowercase()
        val lowerCaseKeyword = keyword.lowercase()

        // 查找第一个匹配的关键字
        val startIndex = lowerCaseText.indexOf(lowerCaseKeyword)

        // 如果找到匹配的关键字
        if (startIndex >= 0) {
            val endIndex = startIndex + keyword.length

            // 设置颜色
            spannableString.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // 将变色后的文本设置到TextView
        textView.text = spannableString
    }

}