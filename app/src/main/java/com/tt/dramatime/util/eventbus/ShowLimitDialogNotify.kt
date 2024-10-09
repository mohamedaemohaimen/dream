package com.tt.dramatime.util.eventbus

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/7/18 上午10:16
 *   Desc : 显示折扣弹窗通知
 *   @param source 5首页弹窗，6商城挽留弹窗 7推送
 * </pre>
 */
class ShowLimitDialogNotify(val loc: Int = 0, val idList: List<String>? = null, val source: Int)