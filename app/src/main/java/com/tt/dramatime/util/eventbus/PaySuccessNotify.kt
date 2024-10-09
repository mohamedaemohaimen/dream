package com.tt.dramatime.util.eventbus


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/6
 *   desc : 支付成功通知
 *   @param unlockType 1单集解锁 2全集解锁 3直接播放
 *   @param showNoticeDialog 是否显示通知弹窗 true显示 false不显示
 * </pre>
 */
class PaySuccessNotify(
    val currentEpisode: Int, val unlockType: Int, val showNoticeDialog: Boolean = false
)