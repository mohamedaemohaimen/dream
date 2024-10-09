package com.tt.dramatime.http.api

import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestCache
import com.hjq.http.model.CacheMode

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 获取搜索页推荐剧集
 * </pre>
 */
class SearchRecommendApi(val type: Int? = null, val pageNum: Int? = null) : IRequestApi ,
    IRequestCache {

    override fun getApi(): String {
        return "home/searchRecommend"
    }

    override fun getCacheMode(): CacheMode {
        return CacheMode.USE_CACHE_FIRST
    }

    override fun getCacheTime(): Long {
        return Long.MAX_VALUE
    }

}