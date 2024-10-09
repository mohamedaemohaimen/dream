package com.tt.dramatime.util.eventbus


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/6
 *   desc : 剧集解锁通知
 *   @param unlockType 1单集解锁 2全集解锁或者多集解锁
 * </pre>
 */
class UnlockNotify(val currentEp: Int, val unlockType: Int)