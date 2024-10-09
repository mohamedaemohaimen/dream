package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 弹窗礼包行为记录接口
 *   @param type 1点击 2购买
 *   @param sourceId 5首页弹窗，6商城挽留弹窗 7推送
 * </pre>
 */
class PopupRecordApi(val id: String, val type: Int, val sourceId: String) : IRequestApi {

    override fun getApi(): String {
        return "popup/record"
    }

    class Bean {

    }
}