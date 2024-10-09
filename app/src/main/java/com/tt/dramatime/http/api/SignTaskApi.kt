package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 获取签到任务
 * </pre>
 */
class SignTaskApi : IRequestApi {

    override fun getApi(): String {
        return "task/signTask"
    }

    class Bean {
        /**
         * signDay : 2
         * list : [{"id":1,"name":"Check-in-1","icon":"http://www.baidu.com","day":1,"integral":20},{"id":2,"name":"Check-in-2","icon":"http://www.baidu.com","day":2,"integral":20}]
         */
        @SerializedName("signDay")
        var signDay: Int = 0

        @SerializedName("list")
        var list: List<ListBean>? = null

        class ListBean {
            /**
             * id : 1
             * name : Check-in-1
             * icon : http://www.baidu.com
             * day : 1
             * integral : 20
             */
            @SerializedName("id")
            var id: Int = 0

            @SerializedName("name")
            var name: String? = null

            @SerializedName("icon")
            var icon: String? = null

            @SerializedName("day")
            var day: Int = 0

            @SerializedName("integral")
            var integral: Int = 0

            @SerializedName("status")
            var status: Int = 0 //状态 0 不可签到 1 已签到 2 可签到
        }
    }
}