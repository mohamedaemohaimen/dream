package com.tt.dramatime.http.api

import com.android.billingclient.api.ProductDetails
import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestCache
import com.hjq.http.model.CacheMode

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 充值
 * </pre>
 */
class RechargeApi(
    val popStatus: Boolean = false, val movieCode: String? = null, val ep: Int? = null
) : IRequestApi, IRequestCache {

    override fun getApi(): String {
        return "store/goods/detail"
    }

    class Bean {
        @SerializedName("subscribeList")
        var subscribeList: MutableList<ProductListBean>? = null

        @SerializedName("rechargeList")
        var rechargeList: MutableList<ProductListBean>? = null

        @SerializedName("unlock")
        var unlock: ProductListBean? = null

        @SerializedName("firstPurchaseStatus")
        var firstPurchaseStatus: Boolean? = null

        @SerializedName("topType")
        var topType: Int = 0 //首位展示商品类型 0订阅 1金币 2解锁

        class ProductListBean {
            /**
             * id : 1747273155058049025
             * title : 月订阅
             * payProductId : miiow_subscriptions_month
             * poster : https://img.miiowapi.com/2024/01/17/15298aaf82754ba9b4502195883e97e7.png
             * cornerPlots : +142%
             * tokensNums : 3600
             * giveTokensNums : 2790
             * timeNums : 1
             * timeCycle : 2
             * sort : 0
             * amount : 25.99
             */
            @SerializedName("id")
            var id: String? = null

            @SerializedName("type")
            var type = 0//类型 0订阅 1金币（内购） 2全剧解锁（内购）

            @SerializedName("name")
            var name: String? = null

            @SerializedName("goodsName")
            var goodsName: String? = null

            @SerializedName("code")
            var code = ""//商品code 周订阅week结尾 月订阅month结尾 年订阅year结尾

            @SerializedName("goodsCode")
            var goodsCode = ""//内购类型商品

            @SerializedName("originalCode")
            var originalCode: String? = null//全剧解锁原价

            @SerializedName("corner")
            var corner: String? = null

            @SerializedName("price")
            var price: String? = null

            @SerializedName("num")
            var num = 0

            @SerializedName("giveNum")
            var giveNum = 0

            @SerializedName("discountType")
            var discountType = 0//0常规 1首购 2促销

            @SerializedName("discountPrice")
            var discountPrice: String? = null

            @SerializedName("desc")
            var desc: String? = null

            @SerializedName("cover")
            var cover: String? = null

            @SerializedName("movieCode")
            var movieCode: String? = null

            @SerializedName("ep")
            var ep: Int? = null

            @SerializedName("promotionIcon")
            var promotionIcon: String? = null

            var productDetails: ProductDetails? = null

            var amount: String = "0.00"

            var currency: String = ""

            var checkStatus = false
        }

    }

    override fun getCacheMode(): CacheMode {
        return CacheMode.USE_CACHE_FIRST
    }

    override fun getCacheTime(): Long {
        return Long.MAX_VALUE
    }
}