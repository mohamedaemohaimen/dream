package com.tt.dramatime.http.db

import android.text.TextUtils
import com.hjq.gson.factory.GsonFactory
import com.tt.dramatime.http.api.UserInfoApi

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/12/7
 *   desc : 用户资料管理
 * </pre>
 */
object UserProfileHelper {

    private var _profile: UserInfoApi.Bean? = null
    val profile: UserInfoApi.Bean get() = _profile!!

    init {
        if (_profile == null) {
            try {
                val jsonStr = MMKVExt.getUserMmkv()?.decodeString(MMKVUserConstant.KEY_PROFILE)
                if (!TextUtils.isEmpty(jsonStr)) {
                    _profile = GsonFactory.getSingletonGson()
                        .fromJson(jsonStr, UserInfoApi.Bean::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (_profile == null) {
            /* 真就为空了、 构建一个 */
            _profile = UserInfoApi.Bean()
        }
    }

    fun setProfile(bean: UserInfoApi.Bean) {
        _profile = bean
        saveProfile()
    }

    fun saveProfile() {
        if (_profile == null) {
            return
        }
        MMKVExt.getUserMmkv()?.encode(MMKVUserConstant.KEY_PROFILE, profile.toString())
    }

    fun getId(): Long {
        return profile.id
    }

    fun getUserId(): Int {
        return profile.userId
    }

    fun setCoinsBonus(coins: Int, bonus: Int) {
        profile.wallet?.balance = coins
        profile.wallet?.integral = bonus
        saveProfile()
    }

    fun setBonus(bonus: Int) {
        profile.wallet?.integral = bonus
        saveProfile()
    }


    fun getCoins(): Int {
        return profile.wallet?.balance ?: 0
    }

    fun getBonus(): Int {
        return profile.wallet?.integral ?: 0
    }

    fun getVipState(): Boolean {
        //Logger.e("getVipState.System.currentTimeMillis():${System.currentTimeMillis()}  profile.expireTimestamp:${profile.expireTimestamp}")
        return System.currentTimeMillis() < profile.expireTimestamp && profile.subscribeStatus == 1
    }

    fun getSubscribeType(): String? {
        return profile.subscribeType
    }

    fun setSubscribeType(subscribeType: String) {
        profile.subscribeType = subscribeType
        saveProfile()
    }

    fun getSignStatus(): Int {
        return profile.signStatus
    }

    fun setSignStatus(signStatus: Int) {
        profile.signStatus = signStatus
        saveProfile()
    }

}