package com.tt.dramatime.http.bean

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 字幕样式
 * </pre>
 */
data class SubtitleStyleBean(
    val fontSize: Float?,
    val position: Float?,
    val strokeColo: String?,
    val textColor: String?,
    val srtPositionRate: Float?
)