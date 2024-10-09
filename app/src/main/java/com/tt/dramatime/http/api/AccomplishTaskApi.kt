package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 完成任务
 * </pre>
 */
class AccomplishTaskApi(val id: Long, val businessType: Int) : IRequestApi {

    override fun getApi(): String {
        return "task/accomplish"
    }

    class Bean {

    }
}