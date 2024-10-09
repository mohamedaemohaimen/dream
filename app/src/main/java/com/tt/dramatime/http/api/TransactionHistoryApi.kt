package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 交易历史
 * </pre>
 */
class TransactionHistoryApi(private val pageNum: Int)  : IRequestApi {

    override fun getApi(): String {
        return "user/wallet/record"
    }

    private val pageSize: Int = 20

    class Bean {
        /**
         * total : 0
         * list : [{"id":0,"action":"string","type":0,"tokenNums":0,"createTime":"2019-08-24T14:15:22Z","createTimeUTC":0}]
         */
        @SerializedName("total")
        var total = 0

        @SerializedName("list")
        var list: List<ListBean>? = null


        class ListBean {
            /**
             * id : 0
             * action : string
             * type : 0
             * tokenNums : 0
             * createTime : 2019-08-24T14:15:22Z
             * createTimeUTC : 0
             */
            @SerializedName("id")
            var id = 0

            @SerializedName("content")
            var action: String? = null

            @SerializedName("type")
            var type = 0// 0收入 1支出

            @SerializedName("num")
            var tokenNums = 0

            @SerializedName("createTime")
            var createTime: String? = null

            @SerializedName("createTimeUTC")
            var createTimeUTC = 0
        }

    }
}