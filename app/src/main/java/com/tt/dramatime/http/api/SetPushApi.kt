package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 设备firebase注册 获取设备推送token
 * </pre>
 */
class SetPushApi(val registrationToken: String?, val pushStatus: Boolean) : IRequestApi {

    override fun getApi(): String {
        return "user/setPush"
    }
}