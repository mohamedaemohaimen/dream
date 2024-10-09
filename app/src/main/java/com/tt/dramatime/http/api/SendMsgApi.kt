package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 发送消息
 * </pre>
 */
class SendMsgApi(val msgType: Int, val content: String) : IRequestApi {

    override fun getApi(): String {
        return "user/sendMsg"
    }

    class Bean {

    }
}