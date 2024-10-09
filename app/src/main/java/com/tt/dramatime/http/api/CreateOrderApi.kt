package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 预创建订单 生成订单号
 * </pre>
 */
class CreateOrderApi(
    val movieCode: String? = null,
    val ep: Int? = null,
    val source: Int,// 0弹窗 1商城 4弹窗续订 5首页弹窗，6商城挽留弹窗
    val goodsCode: String,
    val amount: String,
    val currency: String
) : IRequestApi {

    override fun getApi(): String {
        return "store/order/create"
    }

}