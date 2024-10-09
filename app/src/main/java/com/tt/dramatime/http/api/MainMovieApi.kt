package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 获取广告剧或主推剧
 * </pre>
 */
class MainMovieApi(val data: String?) : IRequestApi {

    override fun getApi(): String {
        return "movie/getMainMovie"
    }

}