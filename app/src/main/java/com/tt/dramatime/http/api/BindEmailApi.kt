package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 保存用户邮箱
 * </pre>
 */
class BindEmailApi(val email: String, val code: String) : IRequestApi {

    override fun getApi(): String {
        return "user/bindEmail"
    }

    class Bean {

    }
}