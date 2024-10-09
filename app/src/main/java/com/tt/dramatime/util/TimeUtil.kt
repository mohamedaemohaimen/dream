package com.tt.dramatime.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/6/25 15:16
 *   Desc : 时间处理；类
 * </pre>
 */
object TimeUtil {
    fun convertTimestampToDateUsingUtil(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }
}