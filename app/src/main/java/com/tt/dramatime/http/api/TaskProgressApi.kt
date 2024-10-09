package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 任务进度上报
 * </pre>
 */
class TaskProgressApi(val businessType: Int, val source: String, val sourceIndex: Int) :
    IRequestApi {

    override fun getApi(): String {
        return "task/progress"
    }

    class Bean {
        /**
         * recordNum : 1
         * taskFinish : true
         * refreshTime : 1719359999999
         */
        @SerializedName("recordNum")
        var recordNum: Int = 0

        @SerializedName("taskFinish")
        var taskFinish: Boolean = false

        @SerializedName("refreshTime")
        var refreshTime: Long = 0
    }
}