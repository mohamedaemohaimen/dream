package com.tt.dramatime.http.api

import com.tt.dramatime.other.AppConfig

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/3/18 10:34
 *   Desc : app用到的url
 * </pre>
 */
class HttpUrls {

    companion object {
        /**备用域名*/
        const val SPARE_HOST_URL = "https://prod.dramatimeapi.com/"

        /**tiktok api域名*/
        const val TT_HOST_URL = "https://business-api.tiktok.com/open_api/"

        /**用户服务*/
        const val SERVICE_URL = "https://www.drama-time.com/privacy/terms_of_service"

        /**用户协议*/
        const val PRIVACY_URL = "https://www.drama-time.com/privacy/policy"

        /**订阅协议*/
        const val RENEWAL_AGREEMENT_URL = "https://www.drama-time.com/privacy/renewal_agreement"

        /**剧集详情*/
        const val MOVIE_DETAIL_URL = "movie/getAppDetail"
        const val AD_MOVIE_DETAIL_URL = "movie/getAdAppDetail"

        /**单集详情*/
        const val EP_DETAIL_URL = "movie/getEpDetail"
        const val AD_EP_DETAIL_URL = "movie/getAdEpDetail"

        fun getH5CompleteUrl(path: String): String {
            return AppConfig.getHostUrl() + path
        }
    }
}