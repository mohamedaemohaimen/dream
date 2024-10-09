package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 获取收藏、观看历史、资源列表
 * </pre>
 */
class MovieListApi : IRequestApi {

    override fun getApi(): String {
        return "movie/history/list"
    }

    private val pageSize: Int = 100

    private val pageNum: Int = 1

    class Bean {
        /**
         * total : 1
         * list : [{"id":"1765268904407924737","movieId":"1747971738132520962","title":"زوجي الملياردير اللطيف","poster":"https://img.miiowtv.com/2024/02/19/cf0ecb72566f4ac9bba859f7cf3cb37c.jpg","summary":"عشية زفافهما، تعرضت غو أنران للخيانة من قبل خطيبها، وبسبب الغضب تزوجت من متسول. كان زوج المتسول وسيمًا وشغوفًا بزوجته. كان يريد في الأصل أن يعيش حياة زوجية عادية، لكنه لم أتوقع أن الشخص الآخر تبين أنه رئيس تنفيذي ملياردير!","tags":"","currentEpisode":80,"watchEpisode":2}]
         */
        @SerializedName("total")
        var total = 0

        @SerializedName("list")
        var list: List<ListBean>? = null


        class ListBean {
            /**
             * id : 1765268904407924737
             * movieId : 1747971738132520962
             * title : زوجي الملياردير اللطيف
             * poster : https://img.miiowtv.com/2024/02/19/cf0ecb72566f4ac9bba859f7cf3cb37c.jpg
             * summary : عشية زفافهما، تعرضت غو أنران للخيانة من قبل خطيبها، وبسبب الغضب تزوجت من متسول. كان زوج المتسول وسيمًا وشغوفًا بزوجته. كان يريد في الأصل أن يعيش حياة زوجية عادية، لكنه لم أتوقع أن الشخص الآخر تبين أنه رئيس تنفيذي ملياردير!
             * tags :
             * currentEpisode : 80
             * watchEpisode : 2
             */
            @SerializedName("id")
            var id: String? = null

            @SerializedName("movieId")
            var movieId: String? = null

            @SerializedName("movieCode")
            var movieCode: String? = null

            @SerializedName("title")
            var title: String? = null

            @SerializedName("poster")
            var poster: String? = null

            @SerializedName("summary")
            var summary: String? = null

            @SerializedName("totalEpisode")
            var totalEpisode: Int? = null

            @SerializedName("watchEpisode")
            var watchEpisode: Int? = null
        }

    }
}