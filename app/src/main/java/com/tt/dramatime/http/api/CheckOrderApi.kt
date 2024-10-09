package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/8
 *   desc : 订单校验
 * </pre>
 */
class CheckOrderApi(
    val packageName: String,
    val orderSn: String,
    val purchaseToken: String,
    val goodsCode: String
) : IRequestApi {

    override fun getApi(): String {
        return "pay/google/checkOrder"
    }
}