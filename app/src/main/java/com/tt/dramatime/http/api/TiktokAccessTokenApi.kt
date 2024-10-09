package com.tt.dramatime.http.api

import com.google.gson.annotations.SerializedName
import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestBodyStrategy
import com.hjq.http.config.IRequestServer
import com.hjq.http.model.RequestBodyType

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 获取TikTok AccessToken接口
 * </pre>
 */
class TiktokAccessTokenApi(val app_id: String, val auth_code: String, val secret: String) :
    IRequestApi, IRequestServer {

    override fun getHost(): String {
        return HttpUrls.TT_HOST_URL
    }

    override fun getBodyType(): IRequestBodyStrategy {
        return RequestBodyType.JSON
    }

    override fun getApi(): String {
        return "v1.3/oauth2/access_token"
    }

    class Bean {
        /**
         * {
         *   "request_id": "20240919074043AA98956CADB290685B13",
         *   "data": {
         *     "access_token": "e4580565bd62fbce8db2004e73c7e159afc444a0",
         *     "advertiser_ids": [
         *         "7377662289052385281",
         *         "7377662312146190337",
         *         "7377662334841569296",
         *         "7381312837806800912",
         *         "7381312861068378113",
         *         "7381312883830931457",
         *         "7390652473964494864",
         *         "7391703018342858769",
         *         "7391742460407578641",
         *         "7397315491708256272",
         *         "7397624502315794449",
         *         "7397624692728692752",
         *         "7398471347803914256",
         *         "7398471525336170513",
         *         "7398471645792436241",
         *         "7398471716248502289",
         *         "7398471879218151440",
         *         "7398471975770882049",
         *         "7398472116527448081",
         *         "7401029048450891792",
         *         "7401029259860557841",
         *         "7401029388545900560",
         *         "7401029482234085377",
         *         "7401029633854373889",
         *         "7401029802897014785",
         *         "7401029879082516497",
         *         "7401030056182611985",
         *         "7401030159467315217",
         *         "7401030327285776385"
         *     ],
         *     "scope": [
         *         18000000,
         *         1,
         *         2,
         *         3,
         *         4,
         *         5,
         *         6,
         *         7,
         *         8,
         *         9,
         *         7043626160646946818,
         *         7050363942434013185,
         *         7294180478502830081,
         *         7336834092152913922,
         *         15000000,
         *         10000,
         *         19000000,
         *         12000000,
         *         16000000,
         *         20000000,
         *         17000000
         *     ]
         * }
         **/
        @SerializedName("access_token")
        var accessToken: String? = null

        @SerializedName("scope")
        var scope: List<Int>? = null

        @SerializedName("advertiser_ids")
        var advertiserIds: List<String>? = null
    }


}