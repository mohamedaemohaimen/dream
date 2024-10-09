# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

# Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# OkHttp3
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn okhttp3.internal.platform.**

# 保持轮子哥全家桶
-keep class com.hjq.** {*;}

# 必须要加上此规则，否则会导致泛型解析失败
-keep public class * implements com.hjq.http.listener.OnHttpListener {
    *;
}
# 保留 Kotlin 反射库
-keep class kotlin.reflect.** { *; }
-keepclassmembers class kotlin.reflect.** { *; }

# 保留 Kotlin 反射需要的元数据
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# 如果你使用了 kotlinx.coroutines 库，也需要添加以下规则
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}

# 腾讯库
-keep class com.tencent.** { *; }

#Google支付
-keep class com.android.vending.billing.**

#键盘弹起优化
-keep class com.effective.android.panel.view.content.*{*;}

#immersionbar
-keep class com.gyf.immersionbar.* {*;}
-dontwarn com.gyf.immersionbar.**

#eventBus 混淆配置
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#TikTok广告统计
-keep class com.tiktok.** { *; }
-keep class com.android.billingclient.api.** { *; }
