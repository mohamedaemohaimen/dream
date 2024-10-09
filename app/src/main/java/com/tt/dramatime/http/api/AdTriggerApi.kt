package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 广告上报
 * </pre>
 */
class AdTriggerApi(
    val adUnitId: String,
    val count: Int,
    val movieId: String?,
    val ep: Int?,
    val traceId: String,
    val type: Int,//观看广告类型 0 观看广告任务 1看广告解锁剧集
    val revenue: Double? = null,//广告收益
    val networkName: String? = null,//广告网络名称
    val adLabel: String? = null//广告类型
) : IRequestApi {

    override fun getApi(): String {
        return "s2s/trigger"
    }

    class Bean {
        /**
         * traceId : string
         * todayUnlockCount : 0
         */
        @SerializedName("traceId")
        var traceId: String? = null

        @SerializedName("todayUnlockCount")
        var todayUnlockCount = 0
    }
}