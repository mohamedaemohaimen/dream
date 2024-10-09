package com.tt.dramatime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hjq.gson.factory.GsonFactory
import com.orhanobut.logger.Logger
import com.tt.dramatime.app.AppActivity
import com.tt.dramatime.http.bean.EpNoticeBean
import com.tt.dramatime.http.bean.LimitDialogNoticeBean
import com.tt.dramatime.ui.activity.BrowserActivity
import com.tt.dramatime.ui.activity.BrowserActivity.Companion.INTENT_KEY_IN_URL
import com.tt.dramatime.ui.activity.HomeActivity
import com.tt.dramatime.ui.activity.me.ContactUsActivity
import com.tt.dramatime.ui.activity.player.BonusActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity.Companion.KEY_MOVIE_ID
import com.tt.dramatime.ui.activity.wallet.StoreActivity
import com.tt.dramatime.util.eventbus.PushTokenNotify
import com.tt.dramatime.util.eventbus.ShowLimitDialogNotify
import org.greenrobot.eventbus.EventBus
import java.util.Random


/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/3/20 9:57
 *   Desc : 一项扩展 FirebaseMessagingService 的服务。除了接收通知外，
 *          如果您还希望在后台应用中进行消息处理，则必须添加此服务。
 *          例如，您需要在前台应用中接收通知、接收数据载荷以及发送上行消息等，就必须扩展此服务。
 *          这个服务只能放在包名根目录下 不然后台点击跳转不生效
 * </pre>
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * 当有新的Firebase token 时的回调
     * 第一次安装app 获取到的 token
     */
    override fun onNewToken(token: String) {
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        Logger.d("FirebaseMessagingService.token==$token")
        EventBus.getDefault().post(PushTokenNotify(token))
    }

    /**
     * 监听推送的消息
     * 三种情况：
     * 1，通知时：
     * 当应用处于前台的时候，推送的消息会走onMessageReceived方法，处于后台时走系统托盘。
     * 2，数据时：
     * 当应用处于前、后台的时候，会走onMessageReceived方法。
     * 3，通知且携带数据：
     * 当应用处于前台的时候，推送的消息会走onMessageReceived方法，处于后台时，通知走系统托盘，数据走Intent 的 extra 中（点击通知栏后）。
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Logger.d("onMessageReceived.From: ${remoteMessage.from} id:${remoteMessage.messageId}")

        var data: Map<String, String>? = null
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Logger.d("onMessageReceived.Message data payload: ${remoteMessage.data}")
            data = remoteMessage.data

            // Check if data needs to be processed by long running job
            /*if (needsToBeScheduled()) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }*/
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            val clickAction = it.clickAction
            if (it.imageUrl != null) {
                Glide.with(this).asBitmap().load(it.imageUrl).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        sendNotification(it.title, it.body, data, resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            } else sendNotification(it.title, it.body, data, null)

            Logger.d("onMessageReceived.Message Notification Body: ${it.body} clickAction:$clickAction")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * 自定义通知
     *
     * @param messageBody
     */
    private fun sendNotification(
        messageTitle: String?, messageBody: String?, data: Map<String, String>?, image: Bitmap?
    ) {
        val intent = prepareIntent(data)

        val notificationManager = getSystemService(NotificationManager::class.java)
        var channelId = ""

        val mMessageTitle = messageTitle ?: "DramaTime"
        val mMessageBody = messageBody ?: "DramaTime"
        // 适配 Android 8.0 通知渠道新特性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                mMessageTitle, mMessageBody, NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.vibrationPattern = longArrayOf(0)
            channel.setSound(null, null)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
            channelId = channel.id
        }
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setContentTitle(messageTitle)
                // 设置通知的文本
                .setContentText(messageBody)
                // 设置通知小图标
                .setSmallIcon(R.drawable.app_logo)
                // 设置通知大图标
                .setLargeIcon(image)
                // 设置通知静音
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                // 设置震动频率
                .setVibrate(longArrayOf(0))
                // 设置声音文件
                .setSound(null)
                // 设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(
            generateUniqueId(), notificationBuilder
                // 设置通知点击之后的意图
                .setContentIntent(/*
                     * 当你创建 PendingIntent 时，Android 系统会为每个 PendingIntent 创建一个唯一的标识符，
                     * 并将其缓存起来以供将来使用。因此，如果你使用相同的 Intent 和标识符创建了多个 PendingIntent，
                     * 那么它们将被视为相同的 PendingIntent，即使你修改了 Intent 的数据也不会生效
                     * 使用 PendingIntent.FLAG_UPDATE_CURRENT 标志，它会更新现有的 PendingIntent，而不是创建一个新的。这样可以确保修改后的数据会被使用
                     */
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        if (Build.VERSION.SDK_INT >= 34) PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_UPDATE_CURRENT
                        else PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                // 设置点击通知后是否自动消失
                .setAutoCancel(true)
                // 是否正在交互中 设置为true 则用不不能关闭该通知
                .setOngoing(false).build()
        )
    }

    private fun generateUniqueId(): Int {
        val timestamp = System.currentTimeMillis()
        val random = Random()
        val randomId = random.nextInt(1000000) // 随机数范围可以根据需要调整
        return timestamp.toInt() + randomId
    }

    private fun prepareIntent(data: Map<String, String>?): Intent {
        val intent: Intent = if (!data.isNullOrEmpty()) {
            Logger.e("handleNotificationClick.path:${data["path"]} query:${data["query"]}")
            when (data["path"]) {
                "/player_new" -> {
                    val mEpNoticeBean = GsonFactory.getSingletonGson().fromJson(
                        data["query"], EpNoticeBean::class.java
                    )
                    Intent(this, PlayerActivity::class.java).putExtra(
                        KEY_MOVIE_ID, mEpNoticeBean.movieId ?: ""
                    ).putExtra(AppActivity.KEY_MESSAGE_ID, data["messageId"])
                }

                "/store" -> Intent(
                    this, StoreActivity::class.java
                ).putExtra(AppActivity.KEY_MESSAGE_ID, data["messageId"])

                "/webview" -> Intent(this, BrowserActivity::class.java).putExtra(
                    INTENT_KEY_IN_URL, data["query"]
                ).putExtra(AppActivity.KEY_MESSAGE_ID, data["messageId"])

                "/chat" -> Intent(
                    this, ContactUsActivity::class.java
                ).putExtra(AppActivity.KEY_MESSAGE_ID, data["messageId"])

                "/bonus" -> Intent(
                    this, BonusActivity::class.java
                ).putExtra(AppActivity.KEY_MESSAGE_ID, data["messageId"])

                "/gift_card" -> {
                    val mLimitDialogNoticeBean = GsonFactory.getSingletonGson().fromJson(
                        data["query"], LimitDialogNoticeBean::class.java
                    )
                    EventBus.getDefault()
                        .post(ShowLimitDialogNotify(0, mLimitDialogNoticeBean.idList, 7))
                    Intent(this, HomeActivity::class.java).putExtra("path", "/gift_card")
                        .putExtra("query", data["query"])
                        .putExtra(AppActivity.KEY_MESSAGE_ID, data["messageId"])
                }

                else -> Intent()
            }
        } else {
            Intent()
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return intent
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

}