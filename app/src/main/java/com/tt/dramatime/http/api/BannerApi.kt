package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.annotation.HttpHeader
import com.hjq.http.annotation.HttpRename
import com.hjq.http.config.IRequestApi
import com.tt.dramatime.http.bean.VideoModel

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 首页Banner
 * </pre>
 */
class BannerApi : IRequestApi {

    override fun getApi(): String {
        return "home/banner"
    }

    @HttpHeader
    @HttpRename("api-version")
    val apiVersion: String = "1.1"

    class Bean {
        /**
         * title : زواج علي الورق
         * poster : https://img.miiowtv.com/2024/02/19/dab234a6a388469ba807a2f22065e395.jpg
         * urlType : 1
         * url : 1747961009128054786
         * seriesDetail : {"movieId":"1747961009128054786","seriesId":"1747979673315287041","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/3f125f1e3270835013693562514/OCoO7EQwpIoA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/3f125f1e3270835013693562514/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null}
         */
        @SerializedName("title")
        var title: String? = null

        @SerializedName("poster")
        var poster: String? = null

        @SerializedName("urlType")
        var urlType: String? = null//（0无跳转 1剧集 2商城 3H5）

        @SerializedName("url")
        var url: String? = null

        @SerializedName("activity")
        var activity: MovieActivityBean? = null

        class MovieActivityBean {
            /**
             * type : string
             * timestamp : string
             * remindStatus : string
             */
            @SerializedName("type")
            var type: Int? = null

            @SerializedName("timestamp")
            var timestamp: Long? = null

            @SerializedName("remindStatus")
            var remindStatus: Int? = null
        }

        @SerializedName("seriesDetail")
        var seriesDetail: VideoModel? = null
    }
}