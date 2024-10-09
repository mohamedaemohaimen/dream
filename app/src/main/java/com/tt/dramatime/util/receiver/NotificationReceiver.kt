package com.tt.dramatime.util.receiver
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.tt.dramatime.R
import com.tt.dramatime.ui.activity.me.SettingActivity

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/8/12 下午2:51
 *   Desc : 新建的Kotlin文件
 * </pre>
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, SettingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "offline_notification_channel")
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("离线通知")
            .setContentText("这是一个本地离线推送通知")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
