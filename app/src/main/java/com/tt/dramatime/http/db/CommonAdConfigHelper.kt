package com.tt.dramatime.http.db

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.hjq.gson.factory.GsonFactory
import com.tt.dramatime.http.api.CommonAdConfigApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/4/3
 *   desc : s2s全局配置管理
 * </pre>
 */
object CommonAdConfigHelper {

    private var _config: List<CommonAdConfigApi.Bean>? = null
    val config: List<CommonAdConfigApi.Bean> get() = _config!!

    init {
        if (_config == null) {
            _config = listOf(CommonAdConfigApi.Bean())
        }
    }

    fun setConfig(bean: List<CommonAdConfigApi.Bean>) {
        _config = bean
    }

    /**
     * 获取广告配置
     * @param type 0 开屏 1 banner 2 解锁
     */
    private fun getAdConfig(type: Int): CommonAdConfigApi.Bean? {
        val data = config.filter { it.type == type }
        return if (data.isNotEmpty()) data[0] else null
    }

    fun getOpenAdEnableStatus(): Boolean {
        return getAdConfig(0)?.enableStatus == true
    }

    fun getOpenAdIntervalFreq(): Int {
        return getAdConfig(0)?.intervalFreq ?: 0
    }

    fun getBannerAdEnableStatus(): Boolean {
        return getAdConfig(1)?.enableStatus == true
    }

}