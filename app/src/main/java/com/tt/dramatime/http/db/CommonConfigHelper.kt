package com.tt.dramatime.http.db

import android.text.TextUtils
import com.hjq.gson.factory.GsonFactory
import com.tt.dramatime.http.api.CommonConfigApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/4/3
 *   desc : s2s全局配置管理
 * </pre>
 */
object CommonConfigHelper {

    private var _config: CommonConfigApi.Bean? = null
    val config: CommonConfigApi.Bean get() = _config!!

    init {
        if (_config == null) {
            try {
                val jsonStr =
                    MMKVExt.getUserMmkv()?.decodeString(MMKVUserConstant.KEY_COMMON_CONFIG)
                if (!TextUtils.isEmpty(jsonStr)) {
                    _config = GsonFactory.getSingletonGson()
                        .fromJson(jsonStr, CommonConfigApi.Bean::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (_config == null) {
            /* 真就为空了、 构建一个 */
            _config = CommonConfigApi.Bean()
        }
    }

    fun setConfig(bean: CommonConfigApi.Bean) {
        _config = bean
        saveConfig()
    }

    fun saveConfig() {
        if (_config == null) {
            return
        }
        MMKVExt.getUserMmkv()?.encode(MMKVUserConstant.KEY_COMMON_CONFIG, config.toString())
    }

    fun getS2SConfig(): CommonConfigApi.Bean.S2SConfigBean {
        return config.s2SConfig ?: CommonConfigApi.Bean.S2SConfigBean()
    }

    fun getEnableStatus(): Boolean {
        return getS2SConfig().androidEnableStatus
    }

    fun getStoreStatus(): Boolean {
        return getS2SConfig().androidStoreStatus
    }

    fun getUnlockNum(): Int {
        return getS2SConfig().unlockNum
    }

    fun getRechargeTip(): String? {
        return config.rechargeTip
    }

}