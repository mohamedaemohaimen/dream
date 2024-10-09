package com.tt.dramatime.util.eventbus


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/6
 *   desc : 观看广告成功通知
 * </pre>
 */
class WatchingAdSuccessNotify(
    val watchNum: Int,
    val todayUnlockCount: Int,
    val currentEpisode: Int,
    val traceId: String,
    val singleUnlock: Int
)