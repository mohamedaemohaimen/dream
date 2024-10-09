package com.tt.dramatime.http.db

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.hjq.gson.factory.GsonFactory
import com.tt.dramatime.http.bean.CheckOrderFailBean

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/5/11
 *   desc : 校验订单失败记录
 * </pre>
 */
object CheckOrderFailHelper {

    private var _checkOrderFailList: MutableList<CheckOrderFailBean>? = null
    private val checkOrderFailList: MutableList<CheckOrderFailBean> get() = _checkOrderFailList!!

    init {
        if (_checkOrderFailList == null) {
            try {
                val jsonStr = MMKVExt.getDurableMMKV()
                    ?.decodeString(MMKVDurableConstant.KEY_FAILED_TO_GRANT_BENEFITS)
                if (!TextUtils.isEmpty(jsonStr)) {
                    _checkOrderFailList = GsonFactory.getSingletonGson().fromJson(
                        jsonStr, object : TypeToken<ArrayList<CheckOrderFailBean>?>() {}.type
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (_checkOrderFailList == null) {/* 真就为空了、 构建一个 */
            _checkOrderFailList = mutableListOf()
        }
    }

    fun addCheckOrderFailBean(mCheckOrderFailBean: CheckOrderFailBean) {
        checkOrderFailList.add(mCheckOrderFailBean)
        saveCheckOrderFailList()
    }

    fun clearCheckOrderFailBean() {
        if (checkOrderFailList.size > 0) {
            checkOrderFailList.clear()
            saveCheckOrderFailList()
        }
    }

    fun getCheckOrderFailBean(): CheckOrderFailBean? {
        return checkOrderFailList.getOrNull(checkOrderFailList.lastIndex)
    }

    private fun saveCheckOrderFailList() {
        if (_checkOrderFailList == null) {
            return
        }
        MMKVExt.getDurableMMKV()?.encode(
            MMKVDurableConstant.KEY_FAILED_TO_GRANT_BENEFITS,
            GsonFactory.getSingletonGson().toJson(checkOrderFailList)
        )
    }

}