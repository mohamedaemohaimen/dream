package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc :根据剧编码获取剧
 * </pre>
 */
class MovieByCodeApi(private val movieCode:String) : IRequestApi {

    override fun getApi(): String {
        return "movie/getMovieByCode"
    }

}