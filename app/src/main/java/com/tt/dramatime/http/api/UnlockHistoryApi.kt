package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 解锁历史
 * </pre>
 */
class UnlockHistoryApi(private val pageNum: Int)  : IRequestApi {

    override fun getApi(): String {
        return "user/unlock/records"
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
             * movieId : 0
             * title : string
             * poster : string
             * summary : string
             * tagList : ["string"]
             * currentEpisode : 0
             * createTime : 2019-08-24T14:15:22Z
             * createTimeUTC : 0
             */
            @SerializedName("id")
            var id = 0

            @SerializedName("movieId")
            var movieId : String? = null

            @SerializedName("movieCode")
            var movieCode : String? = null

            @SerializedName("title")
            var title: String? = null

            @SerializedName("poster")
            var poster: String? = null

            @SerializedName("summary")
            var summary: String? = null

            @SerializedName("currentEpisode")
            var currentEpisode = 0

            @SerializedName("unlockType")
            var unlockType = 0//0单集 1全剧

            @SerializedName("createTime")
            var createTime: String? = null

            @SerializedName("createTimeUTC")
            var createTimeUTC = 0

            @SerializedName("tagList")
            var tagList: List<String>? = null
        }

    }
}