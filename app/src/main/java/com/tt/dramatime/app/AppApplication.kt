package com.tt.dramatime.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.webkit.WebSettings
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.EncodeUtils
import com.bumptech.glide.Glide
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import com.hjq.bar.TitleBar
import com.hjq.gson.factory.GsonFactory
import com.hjq.gson.factory.ParseExceptionCallback
import com.hjq.http.EasyConfig
import com.hjq.http.config.IRequestInterceptor
import com.hjq.http.model.ContentType
import com.hjq.http.model.HttpHeaders
import com.hjq.http.model.HttpMethod
import com.hjq.http.model.HttpParams
import com.hjq.http.request.HttpRequest
import com.hjq.language.MultiLanguages
import com.hjq.language.OnLanguageListener
import com.hjq.toast.Toaster
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.tencent.mmkv.MMKV
import com.tiktok.TikTokBusinessSdk
import com.tt.dramatime.BuildConfig
import com.tt.dramatime.R
import com.tt.dramatime.aop.Log
import com.tt.dramatime.http.api.ApiManager
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_AD_ID
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_CONTENT_LANGUAGE
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_MAD_ID
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.MMKVUserConstant
import com.tt.dramatime.http.model.RequestHandler
import com.tt.dramatime.http.model.RequestServer
import com.tt.dramatime.manager.ActivityManager
import com.tt.dramatime.manager.google.ad.AppOpenAdManager
import com.tt.dramatime.other.AppConfig
import com.tt.dramatime.other.CrashHandler
import com.tt.dramatime.other.DebugLoggerTree
import com.tt.dramatime.other.HomeLoadMoreFooter
import com.tt.dramatime.other.HomeRefreshHeader
import com.tt.dramatime.other.TitleBarStyle
import com.tt.dramatime.other.ToastLogInterceptor
import com.tt.dramatime.other.ToastStyle
import com.tt.dramatime.ui.activity.SplashActivity
import com.tt.dramatime.util.EncryptUtils
import com.tt.dramatime.util.EncryptUtils.generateRandomString
import com.tt.dramatime.util.InstallReferrer
import com.zy.devicelibrary.UtilsApp
import com.zy.devicelibrary.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import java.util.Locale

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 * time   : 2018/10/18
 * desc   : 应用入口
 */
class AppApplication : Application() {

    var appOpenAdManager: AppOpenAdManager? = null

    @Log("启动耗时")
    override fun onCreate() {
        super.onCreate()
        appOpenAdManager = AppOpenAdManager()

        initSdk(this)

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(appContext) { initializationStatus ->

                initializationStatus.adapterStatusMap.forEach { (t, u) ->
                    Logger.d("MobileAds加载成功:$t initializationState:${u.initializationState} description:${u.description}")
                }

                val readyAdapter = initializationStatus.adapterStatusMap.entries.find {
                    it.value.initializationState == AdapterStatus.State.READY
                }
                if (readyAdapter != null) {
                    appOpenAdManager?.loadAd()
                }
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // 清理所有图片内存缓存
        Glide.get(this).onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // 根据手机内存剩余情况清理图片内存缓存
        Glide.get(this).onTrimMemory(level)
    }

    companion object {

        lateinit var appContext: Context

        /**
         * 初始化一些第三方框架
         */
        fun initSdk(application: Application) {
            appContext = application

            // MMKV 初始化
            MMKV.initialize(application)

            // Activity 栈管理初始化
            ActivityManager.getInstance().init(application)

            UtilsApp.init(application)

            // 初始化语种切换框架
            MultiLanguages.init(application)

            MultiLanguages.setOnLanguageListener(object : OnLanguageListener {
                override fun onAppLocaleChange(oldLocale: Locale, newLocale: Locale) {
                    val keys: Array<String?> =
                        ActivityManager.getInstance().getAllActivity().keys.toTypedArray()
                    for (key: String? in keys) {
                        val activity: Activity? =
                            ActivityManager.getInstance().getAllActivity()[key]
                        //启动页不用进行重建
                        if (activity == null || activity.isFinishing || activity is SplashActivity) {
                            continue
                        }
                        activity.recreate()
                    }
                }

                override fun onSystemLocaleChange(oldLocale: Locale, newLocale: Locale) {}
            })

            // FacebookSdk初始化
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.fullyInitialize()
            AppEventsLogger.activateApp(application)

            // Set AppId & TikTok App ID in application code
            val ttConfig = TikTokBusinessSdk.TTConfig(appContext)
                .setAppId("com.tt.dramatime") // Android package name or iOS listing ID, eg: com.sample.app (from Play Store) or 9876543 (from App Store)
                .setTTAppId("7402931796381302801") // TikTok App ID from TikTok Events Manager
            if (BuildConfig.DEBUG) {
                ttConfig.openDebugMode()
                    .setLogLevel(TikTokBusinessSdk.LogLevel.DEBUG) //optional, recommended for diagnostics
            }

            TikTokBusinessSdk.initializeSdk(ttConfig, object : TikTokBusinessSdk.TTInitCallback {
                override fun success() {
                    Logger.d("TikTokBusinessSdk初始化成功")
                }

                override fun fail(code: Int, msg: String?) {
                    Logger.e("TikTokBusinessSdk初始化失败.code$code msg:$msg")
                }
            })
            // TikTok business SDK will actually start sending app events to
            // marketing API when startTrack() function is called. Before this
            // function is called, app events are merely stored locally. Delay
            // calling this function if you need to let the user agree to data terms
            // before actually sending the app events to TikTok.
            TikTokBusinessSdk.startTrack()

            // 设置标题栏初始化器
            TitleBar.setDefaultStyle(TitleBarStyle())

            // 设置全局的 Header 构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context: Context, _: RefreshLayout ->
                HomeRefreshHeader(context)
            }
            // 设置全局的 Footer 构建器
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context: Context, _: RefreshLayout ->
                HomeLoadMoreFooter(context)
            }
            // 设置全局初始化器
            SmartRefreshLayout.setDefaultRefreshInitializer { _: Context, layout: RefreshLayout ->
                // 刷新头部是否跟随内容偏移
                layout.setEnableHeaderTranslationContent(true)
                    // 刷新尾部是否跟随内容偏移
                    .setEnableFooterTranslationContent(true)
                    // 加载更多是否跟随内容偏移
                    .setEnableFooterFollowWhenNoMoreData(true)
                    // 内容不满一页时是否可以上拉加载更多
                    .setEnableLoadMoreWhenContentNotFull(false)
                    // 仿苹果越界效果开关
                    .setEnableOverScrollDrag(false)
            }

            /* Logger */
            val formatStrategy: FormatStrategy =
                PrettyFormatStrategy.newBuilder().tag(AppConstant.LOG_TAG).build()

            Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
                override fun isLoggable(priority: Int, tag: String?): Boolean {
                    return AppConfig.isLogEnable()
                }
            })

            // 初始化吐司
            Toaster.init(application, ToastStyle())
            // 设置调试模式
            Toaster.setDebugMode(AppConfig.isDebug())
            // 设置 Toast 拦截器
            Toaster.setInterceptor(ToastLogInterceptor())

            // 本地异常捕捉
            CrashHandler.register(application)

            // 网络请求框架初始化
            val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

            EasyConfig.with(okHttpClient)
                // 是否打印日志
                .setLogEnabled(AppConfig.isLogEnable())
                // 设置服务器配置
                .setServer(RequestServer())
                // 设置请求处理策略
                .setHandler(RequestHandler(application))
                // 设置请求重试次数
                .setRetryCount(3).setRetryTime(6000).setInterceptor(object : IRequestInterceptor {
                    override fun interceptArguments(
                        httpRequest: HttpRequest<*>, params: HttpParams, headers: HttpHeaders
                    ) {
                        // 添加全局请求头
                        val authorization =
                            MMKVExt.getUserMmkv()?.getString(MMKVUserConstant.KEY_AUTHORIZATION, "")
                        if (authorization?.isEmpty() != true) {
                            headers.put("Authorization", authorization)
                        }
                        headers.put("Package-Name", AppConfig.getPackageName())
                        headers.put("App-Version", AppConfig.getVersionName())
                        headers.put("Version-Build", AppConfig.getVersionCode().toString())
                        headers.put("User-Agent", WebSettings.getDefaultUserAgent(application))
                        headers.put("client-Id", "3b819774da12c8bf8dbb2924c7334e41")
                        val adId = MMKVExt.getDurableMMKV()?.getString(KEY_AD_ID, "")
                        if (adId?.isEmpty() != true) {
                            headers.put("Ad-Id", adId)
                        }
                        val madId = MMKVExt.getDurableMMKV()?.getString(KEY_MAD_ID, "")
                        if (madId?.isEmpty() != true) {
                            headers.put("madId", madId)
                        }
                        headers.put("Os-Type", "android")
                        headers.put(
                            "Device-Language",
                            MultiLanguages.getSystemLanguage(application).language
                        )

                        val contentLanguage =
                            MMKVExt.getDurableMMKV()?.getString(KEY_CONTENT_LANGUAGE, "")

                        val language =
                            if (contentLanguage.isNullOrEmpty()) MultiLanguages.getAppLanguage(
                                application
                            ).language else contentLanguage

                        headers.put("language", language)
                        headers.put("region", AppConfig.getCountry(application))
                        headers.put("Uuid", AppConfig.getWidevineID(application))
                        headers.put("device-Id", FileUtils.getSDDeviceTxt())
                        headers.put("Android-Id", AppConfig.getUUID(application))
                    }

                    override fun interceptResponse(
                        httpRequest: HttpRequest<*>?, response: Response?
                    ): Response {
                        //如果服务端返回encrypt-key则需要去解密
                        response?.headers?.values("encrypt-key")?.let {
                            if (it.isNotEmpty()) {
                                val bytes = response.body?.bytes()

                                bytes?.let { body ->
                                    val encryptData = EncryptUtils.decryptAES(
                                        body, EncryptUtils.decryptRSA(it[0])
                                    )
                                    val newResponseBody =
                                        encryptData.toResponseBody(response.body?.contentType())
                                    return response.newBuilder().body(newResponseBody).build()
                                }
                            }
                        }
                        return super.interceptResponse(httpRequest, response)
                    }

                    override fun interceptRequest(
                        httpRequest: HttpRequest<*>, request: Request
                    ): Request {
                        //POST和PUT请求参数需要加密，请求头需要添加encrypt-key参数
                        val encryption =
                            httpRequest.requestMethod == HttpMethod.POST.toString() || httpRequest.requestMethod == HttpMethod.PUT.toString()
                        if (encryption && ApiManager.encryptionWhiteListApi()
                                .contains(httpRequest.requestApi.api)
                        ) {
                            //生成密钥
                            val randomKey = generateRandomString(32)
                            //密钥使用base64编码
                            val base64Key = EncodeUtils.base64Encode(randomKey)
                            //Rsa公钥加密
                            val aesKey = EncryptUtils.encryptRSA(base64Key)
                            val data = request.body?.toString()
                            val body = EncryptUtils.encryptAES(data, randomKey)
                            //Logger.e("EncryptUtils.body:$body")
                            return request.newBuilder().post(body.toRequestBody(ContentType.JSON))
                                .addHeader("encrypt-key", String(aesKey)).build()
                        }
                        return super.interceptRequest(httpRequest, request)
                    }
                }).into()

            // 设置 Json 解析容错监听
            GsonFactory.setParseExceptionCallback(object : ParseExceptionCallback {
                override fun onParseObjectException(
                    typeToken: TypeToken<*>?, fieldName: String?, jsonToken: JsonToken?
                ) {
                    Firebase.crashlytics.recordException(IllegalArgumentException("类型解析异常：$typeToken#$fieldName，后台返回的类型为：$jsonToken"))
                }

                override fun onParseListItemException(
                    typeToken: TypeToken<*>?, fieldName: String?, listItemJsonToken: JsonToken?
                ) {
                    Firebase.crashlytics.recordException(IllegalArgumentException("类型解析异常：$typeToken#$fieldName，后台返回的类型为：$listItemJsonToken"))
                }

                override fun onParseMapItemException(
                    typeToken: TypeToken<*>?,
                    fieldName: String?,
                    mapItemKey: String?,
                    mapItemJsonToken: JsonToken?
                ) {
                    Firebase.crashlytics.recordException(IllegalArgumentException("类型解析异常：$typeToken#$fieldName，后台返回的类型为：$mapItemJsonToken"))
                }
            })

            // 初始化日志打印
            if (AppConfig.isLogEnable()) {
                Timber.plant(DebugLoggerTree())
            }

            // 获取InstallReferrer Facebook InstallReferrer会一直存在，
            // 测试情况下可能会存在 Facebook InstallReferrer与google InstallReferrer值不一样的情况，因为LP不会写入Facebook InstallReferrer
            InstallReferrer.initReferrerClient(appContext)

            // 注册网络状态变化监听
            ContextCompat.getSystemService(application, ConnectivityManager::class.java)
                ?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onLost(network: Network) {
                        val topActivity: Activity? = ActivityManager.getInstance().getTopActivity()
                        if (topActivity !is LifecycleOwner) {
                            return
                        }
                        val lifecycleOwner: LifecycleOwner = topActivity
                        if (lifecycleOwner.lifecycle.currentState != Lifecycle.State.RESUMED) {
                            return
                        }
                        Toaster.show(R.string.common_network_error)
                    }
                })
        }
    }

    override fun attachBaseContext(base: Context?) {
        // 绑定语种
        super.attachBaseContext(MultiLanguages.attach(base))
    }
}