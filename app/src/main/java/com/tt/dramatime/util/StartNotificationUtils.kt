package com.tt.dramatime.util

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/6/6 20:12
 *   Desc : 跳转推送开关工具类
 * </pre>
 */
object StartNotificationUtils {

    fun getNotificationIntent(packageName: String): Intent {
        val localIntent = Intent()
        //直接跳转到应用通知设置的代码：
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //8.0及以上
            localIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {//5.0以上到8.0以下
            localIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            localIntent.data = Uri.fromParts("package", packageName, null)
        }
        return localIntent
    }
}