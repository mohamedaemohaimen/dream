package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 获取任务
 * </pre>
 */
class TaskListApi : IRequestApi {

    override fun getApi(): String {
        return "task/list"
    }

    class Bean {
        /**
         * businessType : 0
         * finishNum : 0
         * task : {"id":0,"name":"","taskNum":0,"icon":"","integral":0,"url":""}
         * list : [{"id":0,"name":"","taskNum":0,"icon":"","integral":0,"url":""}]
         */
        @SerializedName("businessType")
        var businessType: Int = 0 //业务类型 1 签到任务 2 看广告任务 3开启通知 4App评分任务 7 第三方登录绑定  8绑定邮箱  9观看剧集时长任务 10 签到后观看广告任务

        @SerializedName("type")
        var type: Int = 0 // 0每日任务-完成隐藏按钮 1成长任务-完成隐藏整条item

        @SerializedName("icon")
        var icon: String? = null

        @SerializedName("finishNum")
        var finishNum: Int = 0

        @SerializedName("taskName")
        var taskName: String? = null

        @SerializedName("task")
        var task: TaskBean? = null

        @SerializedName("list")
        var list: MutableList<ListBean>? = null

        class TaskBean {
            /**
             * id : 0
             * name :
             * taskNum : 0
             * icon :
             * integral : 0
             * url :
             */
            @SerializedName("id")
            var id: Long = 0

            @SerializedName("name")
            var name: String? = null

            @SerializedName("taskNum")
            var taskNum: Int = 0

            @SerializedName("icon")
            var icon: String? = null

            @SerializedName("integral")
            var integral: Int = 0

            @SerializedName("url")
            var url: String? = null

            @SerializedName("status")
            var status: Int = 0 //状态 0 未完成 1 已完成 2已领取
        }

        class ListBean {
            /**
             * id : 0
             * name :
             * taskNum : 0
             * icon :
             * integral : 0
             * url :
             */
            @SerializedName("id")
            var id: Long = 0

            @SerializedName("name")
            var name: String? = null

            @SerializedName("taskNum")
            var taskNum: Int = 0

            @SerializedName("icon")
            var icon: String? = null

            @SerializedName("integral")
            var integral: Int = 0

            @SerializedName("url")
            var url: String? = null

            @SerializedName("status")
            var status: Int = 0 //状态 0 未完成 1 已完成 2已领取
        }
    }
}