package com.tt.dramatime.http.bean

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 剧集弹窗实体
 * </pre>
 */
data class EpisodesPlayBean(
    var isPlay: Boolean,
    var isUnlock: Boolean,
    val number: String,
    val isVipFree:Boolean
)