package com.tt.dramatime.app

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/12/7
 *   desc : App常量
 * </pre>
 */
object AppConstant {

    /**
     * 第一页开始下标
     */
    const val FIRST_PAGE = 1

    /**TOKEN过期*/
    const val HTTP_TOKEN_BE_OVERDUE = 401

    /**TOKEN无效*/
    const val HTTP_TOKEN_INVALID = 406

    /**接口访问成功*/
    const val HTTP_OK = 200

    /**日志标识*/
    const val LOG_TAG = "dramatime"

    /**max广告KEY*/
    const val MAX_AD_SDK_KEY =
        "dDURUOtsStHrb0UVRshalirSVPHrcAEfwE66Ka85buv_IqfIfv86NSQQrXgN0q_7dic4rxqcUDJq0BwTiwDC7V"

    /**max广告单元ID*/
    const val MAX_AD_UNIT_ID = "08f16962c6e779ea"

    /**第三方登录标识*/
    const val FACEBOOK_LOGIN = "FACEBOOK"
    const val GOOGLE_LOGIN = "GOOGLE"

    /**UUID登录标识*/
    const val UUID_LOGIN = "UUID"

    var thirdLoginIntegral: Int? = null

    /**广告语言*/
    var adLang: String? = null

    /**广告剧code*/
    var sourceCode: String? = null

}