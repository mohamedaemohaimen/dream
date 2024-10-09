package com.tt.dramatime.http.model

import com.google.gson.annotations.SerializedName
import com.tt.dramatime.app.AppConstant.HTTP_OK
import com.tt.dramatime.app.AppConstant.HTTP_TOKEN_BE_OVERDUE
import com.tt.dramatime.app.AppConstant.HTTP_TOKEN_INVALID

/**
 *  author : Android 轮子哥
 *  github : https://github.com/getActivity/AndroidProject-Kotlin
 *  time   : 2019/12/07
 *  desc   : 统一接口数据结构
 */
open class HttpData<T> {

    /** 请求头  */
    private var headers:  Map<String, String>? = null

    /** 返回码 */
    @SerializedName("code")
    private val code: Int? = null

    /** 提示语 */
    @SerializedName("msg")
    private val msg: String? = null

    /** 数据 */
    private val data: T? = null

    fun setHeaders(headers:  Map<String, String>?) {
        this.headers = headers
    }

    fun getHeaders():  Map<String, String>? {
        return headers
    }

    fun getCode(): Int ?{
        return code
    }

    fun getMessage(): String? {
        return msg
    }

    fun getData(): T? {
        return data
    }

    /**
     * 是否请求成功
     */
    fun isRequestSucceed(): Boolean {
        return code == HTTP_OK
    }

    /**
     * 是否 Token 失效
     */
    fun isTokenFailure(): Boolean {
        return code == HTTP_TOKEN_BE_OVERDUE
    }

    /**
     * 是否 Token 无效
     */
    fun isTokenInvalid(): Boolean {
        return code == HTTP_TOKEN_INVALID
    }
}