package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 删除用户
 * </pre>
 */
class DeleteUserApi : IRequestApi {

    override fun getApi(): String {
        return "auth/cancel"
    }

    class Bean {

    }
}