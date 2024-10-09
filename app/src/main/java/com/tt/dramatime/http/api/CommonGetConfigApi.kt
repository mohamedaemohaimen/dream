package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/4/3
 *   desc : 根据key获取配置
 *   @param configKey ad_config
 * </pre>
 */
class CommonGetConfigApi(private val configKey: String) : IRequestApi {

    override fun getApi(): String {
        return "common/getConfig"
    }

    class Bean {

        @SerializedName("enableStatus")
        var enableStatus: Boolean? = null

    }
}