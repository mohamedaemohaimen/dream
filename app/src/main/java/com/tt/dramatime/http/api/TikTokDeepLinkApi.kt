package com.tt.dramatime.http.api

import com.hjq.http.annotation.HttpHeader
import com.hjq.http.annotation.HttpRename
import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestBodyStrategy
import com.hjq.http.config.IRequestServer
import com.hjq.http.model.RequestBodyType


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : TT延迟深度链接接口
 *   @param tiktok_app_id 7383901465426788369
 *   @param app_id com.tt.dramatime
 *
 * </pre>
 */
class TikTokDeepLinkApi : IRequestApi, IRequestServer {

    override fun getHost(): String {
        return HttpUrls.TT_HOST_URL
    }

    override fun getBodyType(): IRequestBodyStrategy {
        return RequestBodyType.JSON
    }

    override fun getApi(): String {
        return "v1.3/app/s2s_deeplink/"
    }

    @HttpHeader
    @HttpRename("Access-Token")
    private val accessToken: String = "e4580565bd62fbce8db2004e73c7e159afc444a0"

    @HttpRename("tiktok_app_id")
    private val tiktokAppId: String = "7383901465426788369"

    private val timestamp = System.currentTimeMillis()

    private val app = AppBean()

    private val device = DeviceBean()

    class AppBean {
        /**
         * id : YOUR_APP_ID
         */
        var id: String = "com.tt.dramatime"
    }

    class DeviceBean {
        /**
         * platform : DEVICE_PLATFORM
         * idfa : OPTIONAL_IDFA
         * gaid : OPTIONAL_GAID
         * att_status : OPTIONAL_ATT_STATUS
         */
        var platform: String = "Android"

        var idfa: String? = null

        var gaid: String? = null

        var attStatus: String? = null
    }

    class Bean {

    }

}