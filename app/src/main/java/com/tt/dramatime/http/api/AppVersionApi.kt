package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : App版本
 * </pre>
 */
class AppVersionApi : IRequestApi {

    override fun getApi(): String {
        return "basic/appVersion"
    }

    class Bean {
        /**
         * id : 0
         * title : string
         * versions : string
         * versionsCode : string
         * summary : string
         * updateType : 0
         * urlType : string
         * url : string
         * updateStatus : 0
         */
        @SerializedName("id")
        var id = 0

        @SerializedName("title")
        var title: String? = null

        @SerializedName("versions")
        var versions: String? = null

        @SerializedName("versionsCode")
        var versionsCode: String? = null

        @SerializedName("summary")
        var summary: String? = null

        @SerializedName("updateType")
        var updateType = 0 //更新类型 0:强制更新 1:非强制更新

        @SerializedName("urlType")
        var urlType: String? = null

        @SerializedName("url")
        var url: String? = null

        @SerializedName("updateStatus")
        var updateStatus = 0 //是否需要更新 0:不需要更新 1:需要更新
    }
}