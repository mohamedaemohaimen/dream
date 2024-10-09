package com.tt.dramatime.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.AsyncQueryHandler
import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.orhanobut.logger.Logger
import com.tt.dramatime.R
import java.util.Locale

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/4/15 9:50
 *   Desc : 应用角标工具类
 * </pre>
 */
object BadgeUtils {

    private var notificationId = 0

    fun setCount(count: Int, context: Context?): Boolean {
        return if (count >= 0 && context != null) {
            Logger.d("BRAND:" + Build.BRAND)
            when (Build.BRAND.lowercase(Locale.getDefault())) {
               /* "xiaomi" -> {
                    Handler().postDelayed({ setNotificationBadge(count, context) }, 3000)
                    Toast.makeText(context, "请切到后台，3秒后会收到通知", Toast.LENGTH_SHORT).show()
                    true
                }*/

                /*"huawei", "honor" -> setHuaweiBadge(count, context)
                "samsung" -> setSamsungBadge(count, context)
                "oppo" -> setOPPOBadge(count, context) || setOPPOBadge2(count, context)
                "vivo" -> setVivoBadge(count, context)
                "lenovo" -> setZukBadge(count, context)
                "htc" -> setHTCBadge(count, context)
                "sony" -> setSonyBadge(count, context)*/
                else -> setNotificationBadge(count, context)
            }
        } else {
            false
        }
    }

    fun setNotificationBadge(count: Int, context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var channelId = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 8.0之后添加角标需要NotificationChannel
            val channel = NotificationChannel(
                "badge", "badge", NotificationManager.IMPORTANCE_LOW
            )
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
            channelId = channel.id
        }

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setContentTitle("应用角标")
                // 设置通知小图标
                .setSmallIcon(R.drawable.app_logo)
                // 设置通知大图标
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.app_logo))
                // 设置通知静音
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                // 设置震动频率
                .setVibrate(longArrayOf(0))
                // 设置声音文件
                .setSound(null)
                // 设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_MIN)

        val notification: Notification = notificationBuilder
            // 设置通知的文本
            .setContentText("messageBody")
            // 设置通知点击之后的意图
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(),
                    if (Build.VERSION.SDK_INT >= 34) PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
                    else PendingIntent.FLAG_MUTABLE
                )
            )
            // 设置点击通知后是否自动消失
            .setAutoCancel(true)
            .setNumber(count)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            // 是否正在交互中
            //.setOngoing(false)
            .build()
        // 小米
        if (Build.BRAND.equals("xiaomi", ignoreCase = true)) {
            setXiaomiBadge(count, notification)
        }
        notificationManager.notify(notificationId++, notification)
        return true
    }

    private fun setXiaomiBadge(count: Int, notification: Notification) {
        try {
            val field = notification.javaClass.getDeclaredField("extraNotification")
            val extraNotification = field[notification]
            val method = extraNotification.javaClass.getDeclaredMethod(
                "setMessageCount",
                Int::class.javaPrimitiveType
            )
            method.invoke(extraNotification, count)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setHuaweiBadge(count: Int, context: Context): Boolean {
        return try {
            val launchClassName = getLauncherClassName(context)
            if (TextUtils.isEmpty(launchClassName)) {
                return false
            }
            val bundle = Bundle()
            bundle.putString("package", context.packageName)
            bundle.putString("class", launchClassName)
            bundle.putInt("badgenumber", count)
            context.contentResolver.call(
                Uri.parse(
                    "content://com.huawei.android.launcher" +
                            ".settings/badge/"
                ), "change_badge", null, bundle
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun setSamsungBadge(count: Int, context: Context): Boolean {
        return try {
            val launcherClassName = getLauncherClassName(context)
            if (TextUtils.isEmpty(launcherClassName)) {
                return false
            }
            val intent = Intent("android.intent.action.BADGE_COUNT_UPDATE")
            intent.putExtra("badge_count", count)
            intent.putExtra("badge_count_package_name", context.packageName)
            intent.putExtra("badge_count_class_name", launcherClassName)
            context.sendBroadcast(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Deprecated("")
    private fun setOPPOBadge(count: Int, context: Context): Boolean {
        return try {
            val extras = Bundle()
            extras.putInt("app_badge_count", count)
            context.contentResolver.call(
                Uri.parse("content://com.android.badge/badge"),
                "setAppBadgeCount", count.toString(), extras
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Deprecated("")
    private fun setOPPOBadge2(count: Int, context: Context): Boolean {
        return try {
            val intent = Intent("com.oppo.unsettledevent")
            intent.putExtra("packageName", context.packageName)
            intent.putExtra("number", count)
            intent.putExtra("upgradeNumber", count)
            val packageManager = context.packageManager
            val receivers = packageManager.queryBroadcastReceivers(intent, 0)
            if (receivers.size > 0) {
                context.sendBroadcast(intent)
            } else {
                val extras = Bundle()
                extras.putInt("app_badge_count", count)
                context.contentResolver.call(
                    Uri.parse("content://com.android.badge/badge"),
                    "setAppBadgeCount", null, extras
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun setVivoBadge(count: Int, context: Context): Boolean {
        return try {
            val launcherClassName = getLauncherClassName(context)
            if (TextUtils.isEmpty(launcherClassName)) {
                return false
            }
            val intent = Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM")
            intent.putExtra("packageName", context.packageName)
            intent.putExtra("className", launcherClassName)
            intent.putExtra("notificationNum", count)
            context.sendBroadcast(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun setZukBadge(count: Int, context: Context): Boolean {
        return try {
            val extra = Bundle()
            val ids = ArrayList<String>()
            // 以列表形式传递快捷方式id，可以添加多个快捷方式id
            //        ids.add("custom_id_1");
            //        ids.add("custom_id_2");
            extra.putStringArrayList("app_shortcut_custom_id", ids)
            extra.putInt("app_badge_count", count)
            val contentUri = Uri.parse("content://com.android.badge/badge")
            val bundle = context.contentResolver.call(
                contentUri, "setAppBadgeCount", null,
                extra
            )
            bundle != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun setHTCBadge(count: Int, context: Context): Boolean {
        return try {
            val launcherComponentName = getLauncherComponentName(context) ?: return false
            val intent1 = Intent("com.htc.launcher.action.SET_NOTIFICATION")
            intent1.putExtra(
                "com.htc.launcher.extra.COMPONENT", launcherComponentName
                    .flattenToShortString()
            )
            intent1.putExtra("com.htc.launcher.extra.COUNT", count)
            context.sendBroadcast(intent1)
            val intent2 = Intent("com.htc.launcher.action.UPDATE_SHORTCUT")
            intent2.putExtra("packagename", launcherComponentName.packageName)
            intent2.putExtra("count", count)
            context.sendBroadcast(intent2)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun setSonyBadge(count: Int, context: Context): Boolean {
        val launcherClassName = getLauncherClassName(context)
        return if (TextUtils.isEmpty(launcherClassName)) {
            false
        } else try {
            //官方给出方法
            val contentValues = ContentValues()
            contentValues.put("badge_count", count)
            contentValues.put("package_name", context.packageName)
            contentValues.put("activity_name", launcherClassName)
            val asyncQueryHandler = SonyAsyncQueryHandler(
                context
                    .contentResolver
            )
            asyncQueryHandler.startInsert(
                0, null, Uri.parse(
                    "content://com.sonymobile.home" +
                            ".resourceprovider/badge"
                ), contentValues
            )
            true
        } catch (e: Exception) {
            try {
                //网上大部分使用方法
                val intent = Intent("com.sonyericsson.home.action.UPDATE_BADGE")
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", count > 0)
                intent.putExtra(
                    "com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME",
                    launcherClassName
                )
                intent.putExtra(
                    "com.sonyericsson.home.intent.extra.badge.MESSAGE",
                    count.toString()
                )
                intent.putExtra(
                    "com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context
                        .packageName
                )
                context.sendBroadcast(intent)
                true
            } catch (e1: Exception) {
                e1.printStackTrace()
                false
            }
        }
    }

    private fun getLauncherClassName(context: Context): String {
        val launchComponent = getLauncherComponentName(context)
        return launchComponent?.className ?: ""
    }

    private fun getLauncherComponentName(context: Context): ComponentName? {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(
            context
                .packageName
        )
        return launchIntent?.component
    }

    internal class SonyAsyncQueryHandler(cr: ContentResolver?) : AsyncQueryHandler(cr)
}