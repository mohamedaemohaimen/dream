package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 用户语言切换更新
 * </pre>
 */
class UpdateLanguageApi(val language:String) : IRequestApi {

    override fun getApi(): String {
        return "user/updateLanguage"
    }

    class Bean {
        @SerializedName("contentLanguage")
        var contentLanguage: String? = null
    }
}