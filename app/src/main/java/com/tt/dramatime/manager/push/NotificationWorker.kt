package com.tt.dramatime.manager.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tt.dramatime.R
import com.tt.dramatime.ui.activity.me.SettingActivity

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/8/12 下午3:40
 *   Desc : 新建的Kotlin文件
 * </pre>
 */

class NotificationWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // 创建通知
        sendNotification("离线通知", "这是一个本地离线推送通知")
        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建通知渠道 (Android 8.0 及以上)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "offline_notification_channel"
            val channelName = "Offline Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, SettingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, "offline_notification_channel")
            .setSmallIcon(R.drawable.app_logo)  // 你的通知图标
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
