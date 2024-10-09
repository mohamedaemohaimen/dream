package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 拦截据信息
 * </pre>
 */
class InterceptListApi : IRequestApi {

    override fun getApi(): String {
        return "movie/interceptList"
    }

}