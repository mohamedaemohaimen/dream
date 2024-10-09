package com.tt.dramatime.util.eventbus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/6
 *   desc : 剧集选中通知
 * </pre>
 */
data class SelectEpisodesNotify(val number: Int, val smoothScroll: Boolean = false)