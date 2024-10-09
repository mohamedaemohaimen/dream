package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.annotation.HttpHeader
import com.hjq.http.annotation.HttpIgnore
import com.hjq.http.annotation.HttpRename
import com.hjq.http.config.IRequestApi
import com.tt.dramatime.http.bean.VideoModel


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 剧详情
 * </pre>
 */
class MovieDetailApi(val movieId: String, val lang: String? = null) : IRequestApi {

    fun setUrl(url: String): IRequestApi {
        this.url = url
        return this
    }

    @HttpIgnore
    private var url: String = ""

    @HttpHeader
    @HttpRename("api-version")
    val apiVersion: String = "1.1"

    override fun getApi(): String {
        return url
    }

    class Bean {
        /**
         * id : 1747961009128054786
         * showId : 861d4a
         * title : زواج علي الورق
         * poster : https://img.miiowtv.com/2024/02/19/46b4c33e979045db86151aa016f22530.jpg
         * paymentAmount : 50.00
         * totalEpisode : 81
         * viewNum : 9823
         * likeNum : 98559
         * collectNum : 6564
         * likeStatus : false
         * collectStatus : false
         * shareUrl : null
         * list : [{"movieId":"1747961009128054786","seriesId":"1747979673315287041","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/3f125f1e3270835013693562514/OCoO7EQwpIoA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/3f125f1e3270835013693562514/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null,"fileId":null,"playSignature":null,"unlockStatus":true},{"movieId":"1747961009128054786","seriesId":"1747979673327869953","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/f6aa68283270835013668383098/iEzg2uCjjBsA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/f6aa68283270835013668383098/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null,"fileId":null,"playSignature":null,"unlockStatus":true},{"movieId":"1747961009128054786","seriesId":"1747979673336258561","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/f6aafc043270835013668384219/17MI3GgzRFEA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/f6aafc043270835013668384219/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null,"fileId":null,"playSignature":null,"unlockStatus":true},{"movieId":"1747961009128054786","seriesId":"1747979673348841474","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/3f1270043270835013693562907/cMtrUmM91ZwA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/3f1270043270835013693562907/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null,"fileId":null,"playSignature":null,"unlockStatus":true},{"movieId":"1747961009128054786","seriesId":"1747979673365618690","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/01a94c903270835015376994829/QKcIchfoPAgA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/01a94c903270835015376994829/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null,"fileId":null,"playSignature":null,"unlockStatus":true},{"movieId":"1747961009128054786","seriesId":"1747979673407561730","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/f3fc6a003270835013668236084/zbZFakDjZkoA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/f3fc6a003270835013668236084/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null,"fileId":null,"playSignature":null,"unlockStatus":true},{"movieId":"1747961009128054786","seriesId":"1749735862424281090","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/abf294fa3270835015405634526/WUaos9iGd7oA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/abf294fa3270835015405634526/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null,"fileId":null,"playSignature":null,"unlockStatus":true},{"movieId":"1747961009128054786","seriesId":"1749773162738851841","url":"https://vod.miiowtv.com/93109ce8vodger1322272054/fb1aa9643270835015383461668/9URWk9lrrpgA.mp4","cover":"https://vod.miiowtv.com/47383f90vodtransger1322272054/fb1aa9643270835015383461668/coverBySnapshot/coverBySnapshot_10_0.jpg","srtUrl":null,"fileId":null,"playSignature":null,"unlockStatus":true}]
         */
        @SerializedName("id")
        var id: String? = null

        @SerializedName("code")
        var code: String? = null

        @SerializedName("title")
        var title: String? = null

        @SerializedName("type")
        var type: Int = 0 //剧类型，0 常规剧 1 付费点播

        @SerializedName("unlockStartEp")
        var unlockStartEp: Int = 0 //订阅用户剧集解锁开始集数（该集也需要解锁）

        @SerializedName("poster")
        var poster: String? = null

        @SerializedName("subtitleStyle")
        var subtitleStyle: String? = null

        @SerializedName("paymentAmount")
        var paymentAmount = 0

        @SerializedName("totalEpisode")
        var totalEpisode = 0

        @SerializedName("payEpisode")
        var payEpisode = 0

        @SerializedName("viewNum")
        var viewNum = 0

        @SerializedName("likeNum")
        var likeNum = 0

        @SerializedName("collectNum")
        var collectNum = 0

        @SerializedName("likeStatus")
        var likeStatus = false

        @SerializedName("collectStatus")
        var collectStatus = false

        @SerializedName("shareUrl")
        var shareUrl: String? = null

        @SerializedName("list")
        var list: MutableList<VideoModel>? = null

        /**
         * watchHistory : {"movieId":null,"currentEpisode":1,"createTime":1709869050000}
         */
        @SerializedName("watchHistory")
        var watchHistory: WatchHistoryBean? = null

        @SerializedName("activity")
        var activity: MovieActivityBean? = null

        class MovieActivityBean {

            @SerializedName("type")
            var type: Int? = null //0首发剧

            @SerializedName("activityStatus")
            var activityStatus: Int? = null //0未开始 1进行中

            @SerializedName("timestamp")
            var timestamp: Long? = null //时间戳，到毫秒

            @SerializedName("remindStatus")
            var remindStatus: Int? = null //是否提醒 1提醒 0不提醒
        }

        /**
         * s2s : {"id":0,"movieId":0,"enableStatus":true,"unlockEp":0,"viewCount":0}
         */
        @SerializedName("s2s")
        var s2s: S2sBean? = null

        class S2sBean {
            /**
             * enableStatus : true
             * unlockEp : 0
             * viewCount : 0
             */
            @SerializedName("enableStatus")
            var enableStatus = false

            @SerializedName("unlockEp")
            var unlockEp = 0

            @SerializedName("viewCount")
            var viewCount = 0

            @SerializedName("maxUnlock")
            var maxUnlock = 0

            @SerializedName("todayUnlock")
            var todayUnlock = 0

            @SerializedName("singleUnlock")
            var singleUnlock = 1

            @SerializedName("limitStatus")
            var limitStatus = true
        }

        class WatchHistoryBean {
            /**
             * movieId : null
             * currentEpisode : 1
             * createTime : 1709869050000
             */
            @SerializedName("movieId")
            var movieId: String? = null

            @SerializedName("currentEpisode")
            var currentEpisode = 0

            @SerializedName("createTime")
            var createTime: Long = 0
        }

    }
}