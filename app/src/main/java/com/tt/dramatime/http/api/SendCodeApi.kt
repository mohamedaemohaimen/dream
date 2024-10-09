package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 发送邮箱验证码
 * </pre>
 */
class SendCodeApi(val email: String) : IRequestApi {

    override fun getApi(): String {
        return "resource/email/code"
    }

    class Bean {

    }
}