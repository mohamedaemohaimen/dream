package com.tt.dramatime.http.model

import com.hjq.http.config.IRequestBodyStrategy
import com.hjq.http.config.IRequestServer
import com.hjq.http.config.IRequestType
import com.hjq.http.model.RequestBodyType
import com.tt.dramatime.other.AppConfig

/**
 *  author : Android 轮子哥
 *  github : https://github.com/getActivity/AndroidProject-Kotlin
 *  time   : 2020/10/02
 *  desc   : 服务器配置
 */
class RequestServer : IRequestServer, IRequestType {

    override fun getHost(): String {
        return AppConfig.getHostUrl()
    }


    override fun getBodyType(): IRequestBodyStrategy {
        // 以JSON的形式提交参数
        return RequestBodyType.JSON
    }
}