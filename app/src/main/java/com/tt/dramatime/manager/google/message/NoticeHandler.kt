package com.tt.dramatime.manager.google.message

import android.content.Context
import com.hjq.gson.factory.GsonFactory
import com.tt.dramatime.app.AppApplication
import com.tt.dramatime.http.bean.EpNoticeBean
import com.tt.dramatime.manager.player.TXCSDKService
import com.tt.dramatime.ui.activity.BrowserActivity
import com.tt.dramatime.ui.activity.me.ContactUsActivity
import com.tt.dramatime.ui.activity.player.BonusActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.activity.wallet.StoreActivity

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/4/11 14:45
 *   Desc : 推送通知处理
 * </pre>
 */
object NoticeHandler {

    /**
     * @param path 跳转地址
     * @param query 跳转参数
     */
    fun parseNotice(context: Context, path: String, query: String) {
        try {
            when (path) {
                "/player_new" -> {
                    if (TXCSDKService.isInit.not()) TXCSDKService.init(AppApplication.appContext)
                    val mEpNoticeBean = GsonFactory.getSingletonGson().fromJson(
                        query, EpNoticeBean::class.java
                    )
                    PlayerActivity.start(context, mEpNoticeBean.movieId ?: "")
                }

                "/store" -> StoreActivity.start(context)

                "/webview" -> BrowserActivity.start(context, query)

                "/chat" -> ContactUsActivity.start(context)

                "/bonus" -> BonusActivity.start(context)

                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}