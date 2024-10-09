package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 第三方绑定登录
 * </pre>
 */
class AuthBindApi(val token:String,val source:String) : IRequestApi {

    override fun getApi(): String {
        return "auth/bind"
    }

    class Bean {

    }
}