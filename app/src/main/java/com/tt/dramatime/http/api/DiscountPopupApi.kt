package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 折扣弹窗信息查询接口
 *   @param loc 0首页弹窗 1商城关闭
 * </pre>
 */
class DiscountPopupApi(private val loc: Int, private val idList: List<String>? = null) :
    IRequestApi {

    override fun getApi(): String {
        return "popup/detail"
    }

    class Bean {
        /**
         * id : 1812660385372631042
         * name : 测试ios3
         * countdown : 700
         * type : 0
         * intervalFreq : 30
         * goods : {"recharge":null,"unlock":null,"sub":{"id":"1765672511142953064","goodsCode":"sub_week","corner":"99% of people subscribed","goodsName":"Weekly VlP","desc":"\u2022 Unlock All Series for Free\r\n\u2022 Unlock VIP Exclusive Drama\r\n\u2022 Unlock VIP Frame\r\n\u2022 HD Streaming & No Ads","price":"29.99"}}
         */
        @SerializedName("id")
        var id: String? = null

        @SerializedName("name")
        var name: String? = null

        @SerializedName("countdown")
        var countdown: Int = 0

        @SerializedName("type")
        var type: Int = 0

        @SerializedName("intervalFreq")
        var intervalFreq: Int = 0

        @SerializedName("goods")
        var goods: GoodsBean? = null

        class GoodsBean {
            /**
             * recharge : null
             * unlock : null
             * sub : {"id":"1765672511142953064","goodsCode":"sub_week","corner":"99% of people subscribed","goodsName":"Weekly VlP","desc":"\u2022 Unlock All Series for Free\r\n\u2022 Unlock VIP Exclusive Drama\r\n\u2022 Unlock VIP Frame\r\n\u2022 HD Streaming & No Ads","price":"29.99"}
             */
            @SerializedName("subscribe")
            var subscribe: RechargeApi.Bean.ProductListBean? = null

            /**
             * unlock : {"name":"unlock","cover":"http://www.baidu.com","unlockTitle":"unlock all 75 ep","countdown":300,"price":89}
             */
            @SerializedName("unlock")
            var unlock: RechargeApi.Bean.ProductListBean? = null

            @SerializedName("recharge")
            var recharge: MutableList<RechargeApi.Bean.ProductListBean>? = null

        }
    }
}