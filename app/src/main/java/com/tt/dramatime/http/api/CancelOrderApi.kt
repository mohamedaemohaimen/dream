package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 取消订单
 * </pre>
 */
class CancelOrderApi(val orderSn:String) : IRequestApi {

    override fun getApi(): String {
        return "store/order/cancel"
    }

}