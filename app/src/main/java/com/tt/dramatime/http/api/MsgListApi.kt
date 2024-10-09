package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 获取聊天记录
 * </pre>
 */
class MsgListApi : IRequestApi{

    override fun getApi(): String {
        return "user/getMsgList"
    }

    class Bean {
        /**
         * total : 0
         * list : [{"chatTopic":"string","msgType":0,"content":"string","isMe":true,"createTime":"2019-08-24T14:15:22Z","createTimeUTC":0}]
         */
        @SerializedName("total")
        var total = 0

        @SerializedName("list")
        var list: MutableList<MessageBean>? = null


        class MessageBean {
            /**
             * chatTopic : string
             * msgType : 0
             * content : string
             * isMe : true
             * createTime : 2019-08-24T14:15:22Z
             * createTimeUTC : 0
             */
            @SerializedName("chatTopic")
            var chatTopic: String? = null

            @SerializedName("msgType")
            var msgType = 0

            @SerializedName("content")
            var content: String? = null

            @SerializedName("isMe")
            var isMe = false

            @SerializedName("createTime")
            var createTime: String? = null

            @SerializedName("createTimeUTC")
            var createTimeUTC = 0
        }
    }
}