package com.tt.dramatime.http.db

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/7
 *   desc : 与用户绑定的 key ： [MMKVExt.userMMKV]
 * </pre>
 */
class MMKVUserConstant {

    companion object {

        /**登录来源*/
        const val KEY_LOGIN_SOURCE = "key.login.source"

        /**我的资料*/
        const val KEY_PROFILE = "key.profile"

        const val KEY_AUTHORIZATION = "mmkv.key.authorization"

        const val KEY_USER_SIG = "key.userSig"

        const val KEY_APP_ID = "key.appId"

        const val KEY_MY_LIST = "key.my.list"

        const val KEY_S2S_CONFIG = "key.s2s.config"

        const val KEY_COMMON_CONFIG = "key.common.config"

        const val KEY_COMMON_AD_CONFIG = "key.common.ad.config"

    }
}