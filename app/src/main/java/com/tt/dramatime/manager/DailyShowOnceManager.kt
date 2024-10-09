package com.tt.dramatime.manager

import com.tt.dramatime.http.db.MMKVExt
import java.util.Calendar
import java.util.Date

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/4/10 15:27
 *   Desc : 登录弹窗一日显示一次逻辑
 * </pre>
 */
object DailyShowOnceManager {

    private const val KEY_LAST_SHOWN_DATE = "last_shown_date"

    fun shouldShowToday(): Boolean {
        val lastShownTime = MMKVExt.getDurableMMKV()?.decodeLong(KEY_LAST_SHOWN_DATE)
        val todayStart = getTodayStart().time
        return if (lastShownTime != null) lastShownTime < todayStart else true
    }

    fun markAsShown() {
        val todayStart = getTodayStart().time
        MMKVExt.getDurableMMKV()?.encode(KEY_LAST_SHOWN_DATE, todayStart)
    }

    private fun getTodayStart(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}