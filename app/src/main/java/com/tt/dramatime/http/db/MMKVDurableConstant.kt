package com.tt.dramatime.http.db

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/7
 *   desc : 存储持久的数据 key ：[MMKVExt.durableMMKV]
 * </pre>
 */

class MMKVDurableConstant {

    companion object {

        /**设备ID*/
        const val KEY_DEVICE_ID = "mmkv.key.device.id"

        /**ANDROID_ID*/
        const val KEY_ANDROID_ID = "mmkv.key.android.id"

        /**主推剧展示状态*/
        const val KEY_MAIN_VIDEO_DISPLAY_STATUS = "mmkv.key.video.display.status"

        /**视频引导*/
        const val KEY_VIDEO_GUIDE = "mmkv.key.video.guide"

        /**内容语言*/
        const val KEY_CONTENT_LANGUAGE = "mmkv.key.content.language"

        /**最后一次第三方登录来源*/
        const val KEY_LAST_THIRD_LOGIN = "key.last.third.login"

        /**权益发放失败记录*/
        const val KEY_FAILED_TO_GRANT_BENEFITS = "key.failed.to.grant.benefits"

        /**广告注册是否上报*/
        const val KEY_AD_COMPLETION_REGISTER = "key.ad.complete.register"

        /**安装引荐来源*/
        const val KEY_INSTALL_REFERRER = "key.installReferrer"

        /**google AdvertisingIdClient.getAdvertisingIdInfo(getContext()).id*/
        const val KEY_MAD_ID = "key.mad.id"

        /**install referrer 携带的adid*/
        const val KEY_AD_ID = "key.ad.id"

        /**第一次播放 0 不是 1 是*/
        const val KEY_FIRST_PLAY = "key.first.play"

        /**拦截剧显示记录*/
        const val KEY_INTERCEPT_EPISODES_SHOW_TIME = "key.intercept.episodes.show.time"

        /**通知弹窗显示记录*/
        const val KEY_NOTICE_SHOW_TIME = "key.notice.show.time"

        /**首次启动记录*/
        const val KEY_ALREADY_OPEN = "key.already.open"

    }

}