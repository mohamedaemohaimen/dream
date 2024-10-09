package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/4/3
 *   desc : 全局配置
 * </pre>
 */
class CommonConfigApi : IRequestApi {

    override fun getApi(): String {
        return "common/config"
    }

    class Bean {
        /**
         * s2SConfig : {"showDay":0,"iosEnableStatus":true,"androidEnableStatus":true,"androidStoreStatus":true,"iosStoreStatus":true,"unlockNum":0}
         */
        @SerializedName("s2SConfig")
        var s2SConfig: S2SConfigBean? = null

        @SerializedName("rechargeTip")
        var rechargeTip: String? = null

        @SerializedName("clipboardStatus")
        var clipboardStatus = false

        class S2SConfigBean {
            /**
             * showDay : 0
             * iosEnableStatus : true
             * androidEnableStatus : true
             * androidStoreStatus : true
             * iosStoreStatus : true
             * unlockNum : 0
             */
            @SerializedName("showDay")
            var showDay = 0

            @SerializedName("iosEnableStatus")
            var iosEnableStatus = false

            @SerializedName("androidEnableStatus")
            var androidEnableStatus = false

            @SerializedName("androidStoreStatus")
            var androidStoreStatus = false

            @SerializedName("iosStoreStatus")
            var iosStoreStatus = false

            @SerializedName("unlockNum")
            var unlockNum = 0
        }

    }
}