package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 领取任务奖励
 * </pre>
 */
class TaskReceiveApi(val id: Long, val businessType: Int) : IRequestApi {

    override fun getApi(): String {
        return "task/receive"
    }

    class Bean {
        @SerializedName("total")
        var total = 0
    }
}