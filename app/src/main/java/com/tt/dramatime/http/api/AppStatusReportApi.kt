package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 用户app切换后台上报
 * </pre>
 */
class AppStatusReportApi(val scene: Int = 0, val type: Int) : IRequestApi {

    override fun getApi(): String {
        return "user/appStatusReport"
    }

    class Bean {

    }
}