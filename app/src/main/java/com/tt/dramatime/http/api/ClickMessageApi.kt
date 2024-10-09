package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 消息点击回调
 * </pre>
 */
class ClickMessageApi(val messageId: String) : IRequestApi {

    override fun getApi(): String {
        return "auth/clickMessage"
    }

    class Bean
}