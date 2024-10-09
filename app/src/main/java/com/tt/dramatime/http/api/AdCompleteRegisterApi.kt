package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestBodyStrategy
import com.hjq.http.config.IRequestServer
import com.hjq.http.model.RequestBodyType

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 广告注册上报
 * </pre>
 */
class AdCompleteRegisterApi(
    val userId: Int, val referrer: String, val clipboard: String?
) : IRequestApi, IRequestServer {

    override fun getHost(): String {
        return "https://ad-api.drama-time.com/"
    }

    override fun getBodyType(): IRequestBodyStrategy {
        return RequestBodyType.JSON
    }

    override fun getApi(): String {
        return "api/event/completeRegister"
    }

}