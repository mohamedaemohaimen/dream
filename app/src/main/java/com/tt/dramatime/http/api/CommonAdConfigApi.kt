package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/4/3
 *   desc : 获取IAA配置
 * </pre>
 */
class CommonAdConfigApi : IRequestApi {

    override fun getApi(): String {
        return "common/adConfig"
    }

    class Bean {

        @SerializedName("enableStatus")
        var enableStatus = false

        @SerializedName("type")
        var type: Int? = null

        @SerializedName("intervalFreq")
        var intervalFreq: Int? = null

    }
}