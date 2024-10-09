package com.tt.dramatime.http.excepetion

import com.hjq.http.exception.HttpException
import com.tt.dramatime.http.model.HttpData


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/1/4
 *   desc : 返回结果异常
 * </pre>
 */
class ResultException : HttpException {
    private val mData: HttpData<*>

    constructor(message: String?, data: HttpData<*>) : super(message) {
        mData = data
    }

    constructor(message: String?, cause: Throwable?, data: HttpData<*>) : super(message, cause) {
        mData = data
    }

    fun getHttpData(): HttpData<*> {
        return mData
    }
}