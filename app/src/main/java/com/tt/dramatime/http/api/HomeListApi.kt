package com.tt.dramatime.http.api

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 首页列表
 * </pre>
 */
class HomeListApi(val code: String? = null, val pageNum: Int? = null) : IRequestApi {

    override fun getApi(): String {
        return "home/columns"
    }

    class Bean() : Parcelable {
        /**
         * title : النجوم الصاعدة
         * code : Rising_Stars
         * blockStyle : 4
         * movieList : [{"movieId":"1747971738132520962","title":"زوجي الملياردير اللطيف","poster":"https://img.miiowtv.com/2024/02/19/cf0ecb72566f4ac9bba859f7cf3cb37c.jpg","summary":"عشية زفافهما، تعرضت غو أنران للخيانة من قبل خطيبها، وبسبب الغضب تزوجت من متسول. كان زوج المتسول وسيمًا وشغوفًا بزوجته. كان يريد في الأصل أن يعيش حياة زوجية عادية، لكنه لم أتوقع أن الشخص الآخر تبين أنه رئيس تنفيذي ملياردير!","tagList":[],"seriesDetail":null}]
         */
        @SerializedName("title")
        var title: String? = null

        @SerializedName("code")
        var code: String? = null

        var pageNum = 1

        /**
         * 栏目样式
         * 1=》上下图文-左右滑动 2=》左图右文-上下划动 3=》图下文-双排上下划动
         * 4=》上视频下剧集信息,左右划动 5=》排行榜 6=》图下文-三排上下划动 7=》首发剧
         */
        @SerializedName("blockStyle")
        var blockStyle: String? = null

        @SerializedName("pageStatus")
        var pageStatus: Boolean? = null

        @SerializedName("movieList")
        var movieList: ArrayList<MovieListBean>? = null

        constructor(parcel: Parcel) : this() {
            title = parcel.readString()
            code = parcel.readString()
            pageNum = parcel.readInt()
            blockStyle = parcel.readString()
            pageStatus = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
            movieList = parcel.createTypedArrayList(MovieListBean.CREATOR)
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeString(code)
            parcel.writeInt(pageNum)
            parcel.writeString(blockStyle)
            parcel.writeValue(pageStatus)
            parcel.writeTypedList(movieList)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Bean> {
            override fun createFromParcel(parcel: Parcel): Bean {
                return Bean(parcel)
            }

            override fun newArray(size: Int): Array<Bean?> {
                return arrayOfNulls(size)
            }
        }


        class MovieListBean() : Parcelable {
            /**
             * movieId : 1747971738132520962
             * title : زوجي الملياردير اللطيف
             * poster : https://img.miiowtv.com/2024/02/19/cf0ecb72566f4ac9bba859f7cf3cb37c.jpg
             * summary : عشية زفافهما، تعرضت غو أنران للخيانة من قبل خطيبها، وبسبب الغضب تزوجت من متسول. كان زوج المتسول وسيمًا وشغوفًا بزوجته. كان يريد في الأصل أن يعيش حياة زوجية عادية، لكنه لم أتوقع أن الشخص الآخر تبين أنه رئيس تنفيذي ملياردير!
             * tagList : []
             * seriesDetail : null
             */
            @SerializedName("movieId")
            var movieId: String? = null

            @SerializedName("title")
            var title: String? = null

            @SerializedName("poster")
            var poster: String? = null

            @SerializedName("summary")
            var summary: String? = null

            @SerializedName("totalEpisode")
            var totalEpisode: Int? = null

            @SerializedName("tagList")
            var tagList: List<String>? = null

            @SerializedName("corner")
            var corner: CornerBean? = null

            /**视频目录里不需要显示标签 就不序列化了*/
            class CornerBean {
                /**
                 * name : string
                 * image : string
                 */
                @SerializedName("name")
                var name: String? = null

                @SerializedName("image")
                var image: String? = null
            }

            @SerializedName("activity")
            var activity: MovieActivityBean? = null

            class MovieActivityBean {
                /**
                 * type : string
                 * timestamp : string
                 * remindStatus : string
                 */
                @SerializedName("type")
                var type: Int? = null //0首发剧

                @SerializedName("timestamp")
                var timestamp: Long? = null

                @SerializedName("remindStatus")
                var remindStatus: Int? = 0 //是否提醒 1提醒 0不提醒
            }

            constructor(parcel: Parcel) : this() {
                movieId = parcel.readString()
                title = parcel.readString()
                poster = parcel.readString()
                summary = parcel.readString()
                totalEpisode = parcel.readValue(Int::class.java.classLoader) as? Int
                tagList = parcel.createStringArrayList()
                //corner = parcel.readParcelable(CornerBean::class.java.classLoader)
            }

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(movieId)
                parcel.writeString(title)
                parcel.writeString(poster)
                parcel.writeString(summary)
                parcel.writeValue(totalEpisode)
                parcel.writeStringList(tagList)
                //parcel.writeParcelable(corner, flags)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<MovieListBean> {
                override fun createFromParcel(parcel: Parcel): MovieListBean {
                    return MovieListBean(parcel)
                }

                override fun newArray(size: Int): Array<MovieListBean?> {
                    return arrayOfNulls(size)
                }
            }

        }


    }
}