package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 退出登录
 * </pre>
 */
class LogoutApi : IRequestApi {

    override fun getApi(): String {
        return "auth/v2/logout"
    }
}