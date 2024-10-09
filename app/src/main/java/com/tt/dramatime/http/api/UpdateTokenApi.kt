package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : token更新
 * </pre>
 */
class UpdateTokenApi : IRequestApi {

    override fun getApi(): String {
        return "auth/updateToken"
    }

}