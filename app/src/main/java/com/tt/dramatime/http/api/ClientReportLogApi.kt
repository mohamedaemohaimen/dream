package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/4/17
 *   desc : 客户端上报日志
 * </pre>
 */
class ClientReportLogApi(val content: String, val type: String) : IRequestApi {

    override fun getApi(): String {
        return "basic/clientReportLog"
    }

    class Bean {

    }
}