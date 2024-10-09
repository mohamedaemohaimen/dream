package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 解锁视频
 * </pre>
 */
class UnlockMovieApi(
    val movieCode: String, val episode: Int, val scene: String = "", val traceId: String? = null
) : IRequestApi {

    override fun getApi(): String {
        return "movie/unlock"
    }

}