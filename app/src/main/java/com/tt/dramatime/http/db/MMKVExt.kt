package com.tt.dramatime.http.db

import com.tencent.mmkv.MMKV

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/12/8
 *   desc : MMKV存储
 * </pre>
 */
object MMKVExt {

    /**
     * mmkv 分区 key : 持久数据、退出登录不清除
     */
    private const val DISK_ID_DURABLE = "mmkv.disk.id.durable"

    /**
     * mmkv 分区 key : 与登录用户绑定的数据、退出登录时清除
     */
    private const val DISK_ID_USER = "mmkv.disk.id.user"

    @Volatile
    private var durableMMKV: MMKV? = null

    @Volatile
    private var userMMKV: MMKV? = null

    /**
     * mmkv 绑定用户的数据 ， 退出登录需要清除的
     *
     * KEY 统一放在 [com.tt.dramatime.http.db.MMKVUserConstant]
     *
     */
    fun getUserMmkv(): MMKV? {
        if (userMMKV == null) {
            synchronized(MMKVExt::class.java) {
                if (userMMKV == null) {
                    userMMKV = MMKV.mmkvWithID(DISK_ID_USER)
                }
            }
        }
        return userMMKV
    }

    /**
     * mmkv 存储持久的数据， 不与账号绑定的数据； 退出登录不清除的数据
     *
     * KEY 统一放在 [com.tt.dramatime.http.db.MMKVDurableConstant]
     */
    fun getDurableMMKV(): MMKV? {
        if (durableMMKV == null) {
            synchronized(MMKVExt::class.java) {
                if (durableMMKV == null) {
                    durableMMKV = MMKV.mmkvWithID(DISK_ID_DURABLE)
                }
            }
        }
        return durableMMKV
    }

}