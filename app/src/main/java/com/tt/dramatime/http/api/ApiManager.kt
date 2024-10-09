package com.tt.dramatime.http.api

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/8/8 下午5:41
 *   Desc : 需要接口参数加密的名单
 * </pre>
 */
object ApiManager {
    fun encryptionWhiteListApi(): List<String> {
        return arrayListOf(
            "movie/unlock",
            "movie/history/report",
            "popup/record",
            "task/sign",
            "task/accomplish",
            "task/receive",
            "task/progress",
            "s2s/trigger",
            "store/order/create",
            "basic/clientReportLog",
            "pay/google/checkOrder",
            "movie/remind",
            "user/appStatusReport",
        )
    }
}