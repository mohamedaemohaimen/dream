package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 观看记录上报
 * </pre>
 */
class MovieCollectApi(
    val movieCode: String, val episode: Int, val lastEpisode: Int?, val playStatus: Int
) : IRequestApi {

    override fun getApi(): String {
        return "movie/history/report"
    }

    class Bean {

    }
}