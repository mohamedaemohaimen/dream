package com.tt.dramatime.util.eventbus

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/3/19 10:15
 *   Desc : 登录失效通知
 *   @param updateToken 不个更新就直接用UUID登录
 * </pre>
 */
class LoginFailedNotify(val updateToken:Boolean)