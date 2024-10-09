package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.EasyConfig
import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 设备唯一标识登录
 * </pre>
 */
class UuidLoginApi(var uuid:String) : IRequestApi , IRequestClient {

    override fun getApi(): String {
        return "auth/uuidLogin"
    }

    class Bean {
        /**
         * accessToken : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb2dpbiIsImxvZ2luSWQiOiJhcHBfdXNlcjoxNzYzNTMwNTYyNDk0NTAwODY2Iiwicm5TdHIiOiJoUUNad0plWGg3OHVFS3hPbGNmcFYwMjRaN2I0WnNiTyIsImNsaWVudElkIjoiMzhkZTVlNGMxNmQzNDdiNGFkNzM0MDA1ODRkMWNiNWUiLCJ1c2VySWQiOjE3NjM1MzA1NjI0OTQ1MDA4NjZ9.WVZSIcEQcFqV03uJPj0m7z0ugXMQpm1gAq3MH8oxf1I
         * expireIn : 604799
         * clientId : 38de5e4c16d347b4ad73400584d1cb5e
         * viewLanguageList : ["英语","阿拉伯语"]
         */
        @SerializedName("accessToken")
        var accessToken: String? = null

        @SerializedName("expireIn")
        var expireIn = 0

        @SerializedName("clientId")
        var clientId: String? = null

        @SerializedName("contentLanguage")
        var contentLanguage: String? = null

        @SerializedName("viewLanguageList")
        var viewLanguageList: List<String>? = null
    }

    override fun getOkHttpClient(): OkHttpClient {
        val builder = EasyConfig.getInstance().client.newBuilder()
        builder.readTimeout(5000, TimeUnit.MILLISECONDS)
        builder.writeTimeout(5000, TimeUnit.MILLISECONDS)
        builder.connectTimeout(5000, TimeUnit.MILLISECONDS)
        return builder.build()
    }
}