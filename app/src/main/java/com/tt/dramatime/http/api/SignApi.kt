package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 签到
 * </pre>
 */
class SignApi(private val day: Int) : IRequestApi {

    override fun getApi(): String {
        return "task/sign"
    }

    class Bean {

    }
}