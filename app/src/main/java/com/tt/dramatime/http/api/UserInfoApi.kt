package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/1/6
 *   desc : 个人主页
 * </pre>
 */
class UserInfoApi : IRequestApi {

    override fun getApi(): String {
        return "auth/getInfo"
    }

    class Bean {
        /**
         * id : 0
         * userId : 0
         * source : string
         * showId : 0
         * username : string
         * nickname : string
         * avatar : string
         * language : string
         * region : string
         * coins : 0
         * bonus : 0
         * subscribeState : 0
         * subscribeId : 0
         * subscribeTitle : string
         * expireTime : 2019-08-24T14:15:22Z
         * expireTimeUTC : 0
         * pushStatus : 0
         * loginTime : 2019-08-24T14:15:22Z
         * userAgent : string
         * clientBuild : string
         */
        @SerializedName("id")
        var id: Long = 0

        @SerializedName("userId")
        var userId = 0

        @SerializedName("source")
        var source: String? = null

        @SerializedName("username")
        var username: String? = null

        @SerializedName("nickname")
        var nickname: String? = null

        @SerializedName("avatar")
        var avatar: String? = null

        @SerializedName("language")
        var language: String? = null

        @SerializedName("region")
        var region: String? = null

        @SerializedName("wallet")
        var wallet: WalletBalanceApi.Bean? = null

        @SerializedName("subType")
        var subscribeType: String? = null //订阅类型 week结尾 周订阅 month结尾 月订阅

        @SerializedName("subscribeStatus")
        var subscribeStatus: Int = 0 //订阅状态 0 未订阅 1订阅中 2订阅过期

        @SerializedName("expireTime")
        var expireTime: String? = null //订阅到期时间

        @SerializedName("expireTimestamp")
        var expireTimestamp: Long = 0 //订阅到期时间-时间戳

        @SerializedName("loginTime")
        var loginTime: String? = null

        @SerializedName("signStatus")
        var signStatus = -1 // 签到状态 0 未签到 1 已签到


    }
}