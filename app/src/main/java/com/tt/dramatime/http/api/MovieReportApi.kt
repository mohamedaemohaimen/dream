package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 剧集举报
 * </pre>
 */
class MovieReportApi(
    val movieVideosId: String, val type: String, val content: String, val email: String
) : IRequestApi {

    override fun getApi(): String {
        return "movie/report"
    }

    class Bean {

    }
}