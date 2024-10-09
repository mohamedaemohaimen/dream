package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 订阅剧集提醒
 * </pre>
 */
class MovieRemindApi(val movieId: String, val type: Int) : IRequestApi {

    override fun getApi(): String {
        return "movie/remind"
    }

    class Bean {

    }
}