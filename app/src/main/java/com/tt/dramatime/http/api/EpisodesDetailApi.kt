package com.tt.dramatime.http.api

import com.hjq.http.EasyConfig
import com.hjq.http.annotation.HttpIgnore
import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 剧集详情
 * </pre>
 */
class EpisodesDetailApi(val movieId: String, val episodes: Int, val lang: String? = null) :
    IRequestApi, IRequestClient {

    fun setUrl(url: String): IRequestApi {
        this.url = url
        return this
    }

    @HttpIgnore
    private var url: String = ""

    override fun getApi(): String {
        return url
    }


    override fun getOkHttpClient(): OkHttpClient {
        val builder = EasyConfig.getInstance().client.newBuilder()
        builder.readTimeout(6000, TimeUnit.MILLISECONDS)
        builder.writeTimeout(6000, TimeUnit.MILLISECONDS)
        builder.connectTimeout(6000, TimeUnit.MILLISECONDS)
        builder.retryOnConnectionFailure(true)
        return builder.build()
    }

}