package com.tt.dramatime.ui.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.NotificationUtils
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.gyf.immersionbar.ImmersionBar
import com.hjq.gson.factory.GsonFactory
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.tt.base.FragmentPagerAdapter
import com.tt.dramatime.R
import com.tt.dramatime.app.AppActivity
import com.tt.dramatime.app.AppConstant.MAX_AD_SDK_KEY
import com.tt.dramatime.app.AppConstant.thirdLoginIntegral
import com.tt.dramatime.app.AppFragment
import com.tt.dramatime.http.api.AppStatusReportApi
import com.tt.dramatime.http.api.AppVersionApi
import com.tt.dramatime.http.api.HttpUrls
import com.tt.dramatime.http.api.TaskListApi
import com.tt.dramatime.http.bean.LimitDialogNoticeBean
import com.tt.dramatime.http.db.MMKVDurableConstant
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.ActivityManager
import com.tt.dramatime.manager.google.message.NoticeHandler
import com.tt.dramatime.manager.google.pay.GoogleBillingManager
import com.tt.dramatime.manager.player.TXCSDKService
import com.tt.dramatime.manager.push.NotificationWorker
import com.tt.dramatime.ui.adapter.NavigationAdapter
import com.tt.dramatime.ui.dialog.UpdateDialog
import com.tt.dramatime.ui.fragment.BonusFragment
import com.tt.dramatime.ui.fragment.HomeFragment
import com.tt.dramatime.ui.fragment.MeFragment
import com.tt.dramatime.ui.fragment.MyListFragment
import com.tt.dramatime.util.eventbus.LoginAgainNotify
import com.tt.dramatime.util.eventbus.ShowLimitDialogNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Calendar
import java.util.concurrent.TimeUnit


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 首页界面
 * </pre>
 */
class HomeActivity : AppActivity(), NavigationAdapter.OnNavigationListener,
    ActivityManager.ApplicationLifecycleCallback {

    companion object {

        private const val INTENT_KEY_IN_FRAGMENT_INDEX: String = "fragmentIndex"
        private const val INTENT_KEY_IN_FRAGMENT_CLASS: String = "fragmentClass"
        private const val INTENT_KEY_IN_LOGIN_STATUS: String = "loginStatus"

        /**
         * @param loginStatus 0默认值一般由推送进入 1 已登陆 2未登录
         */
        @JvmOverloads
        fun start(
            context: Context,
            fragmentClass: Class<out AppFragment<*>?>? = HomeFragment::class.java,
            loginStatus: Int = 1
        ) {
            val intent = Intent(context, HomeActivity::class.java)
            intent.putExtra(INTENT_KEY_IN_FRAGMENT_CLASS, fragmentClass)
            intent.putExtra(INTENT_KEY_IN_LOGIN_STATUS, loginStatus)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val viewPager: ViewPager? by lazy { findViewById(R.id.vp_home_pager) }
    private val navigationView: RecyclerView? by lazy { findViewById(R.id.rv_home_navigation) }
    private var navigationAdapter: NavigationAdapter? = null
    private var pagerAdapter: FragmentPagerAdapter<AppFragment<*>>? = null

    private var pushEnable: Boolean? = null

    var loginStatus: Int? = null

    var clipboardText = ""

    var idList: List<String>? = null

    private var homeIconTranslationTime: Long = 0

    private var appUpdateManager: AppUpdateManager? = null

    private var mUpdateType = AppUpdateType.FLEXIBLE

    private var mAppVersionBean: AppVersionApi.Bean? = null

    override fun getLayoutId(): Int {
        return R.layout.home_activity
    }

    override fun initBefore() {
        ActivityManager.getInstance().finishAllActivities(HomeActivity::class.java)
    }

    override fun initView() {
        sdkInit()

        loginStatus = getInt(INTENT_KEY_IN_LOGIN_STATUS)

        navigationAdapter = NavigationAdapter(this).apply {
            addItem(
                NavigationAdapter.MenuItem(
                    getString(R.string.home_nav_index),
                    ContextCompat.getDrawable(this@HomeActivity, R.drawable.home_home_selector)
                )
            )
            addItem(
                NavigationAdapter.MenuItem(
                    getString(R.string.home_nav_mylist),
                    ContextCompat.getDrawable(this@HomeActivity, R.drawable.home_mylist_selector)
                )
            )
            addItem(
                NavigationAdapter.MenuItem(
                    getString(R.string.home_nav_bonus),
                    ContextCompat.getDrawable(this@HomeActivity, R.drawable.home_bonus_selector)
                )
            )
            addItem(
                NavigationAdapter.MenuItem(
                    getString(R.string.home_nav_me),
                    ContextCompat.getDrawable(this@HomeActivity, R.drawable.home_me_selector)
                )
            )
            setOnNavigationListener(this@HomeActivity)

            navigationView?.adapter = this
        }

        pagerAdapter = FragmentPagerAdapter<AppFragment<*>>(this).apply {
            addFragment(HomeFragment.newInstance())
            addFragment(MyListFragment.newInstance())
            addFragment(BonusFragment.newInstance())
            addFragment(MeFragment.newInstance())
            viewPager?.adapter = this
            viewPager?.offscreenPageLimit = 2
        }

        val count = pagerAdapter?.count ?: 0
        if (count > 0) {
            val homeFragment = (pagerAdapter?.getItem(0) as HomeFragment)
            if (homeFragment.isAdded) {
                homeFragment.loading = false
            }
        }

        ActivityManager.getInstance().registerApplicationLifecycleCallback(this)

        onNewIntent(intent)
    }

    override fun initData() {
        pushEnable = NotificationUtils.areNotificationsEnabled()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                MMKVExt.getDurableMMKV()?.encode(
                    MMKVDurableConstant.KEY_MAD_ID,
                    AdvertisingIdClient.getAdvertisingIdInfo(getContext()).id
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        getAppVersion()
        getThirdLoginIntegral()
    }

    override fun onResume() {
        super.onResume()
        GoogleApiAvailability().makeGooglePlayServicesAvailable(this)

        if (mUpdateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
                Logger.d("getAppVersion.onResume.appUpdateInfo: ${appUpdateInfo.updateAvailability()}")
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
        }

        //Android10 以上要延时才能获取到粘贴板数据  获取剪贴板审核被拒
        //postDelayed({ getClipboardText() }, 200)
    }

    private fun sdkInit() {
        if (EventBus.getDefault().isRegistered(this).not()) {
            EventBus.getDefault().register(this)
        }

        if (TXCSDKService.isInit.not()) TXCSDKService.init(applicationContext)

        /*AppLinkData.fetchDeferredAppLinkData(this) { appLinkData ->
            // 处理延迟深度链接
            appLinkData?.targetUri?.let { uri ->
                Logger.e("AppLinkData.fetchDeferredAppLinkData: $uri")
                // 例如导航到 dramatime://app/?sourceCode=c3191f
            }
        }*/

        //建立连接
        GoogleBillingManager.createClient()

        //AppLovinSdk初始化
        //Make sure to set the mediation provider value to "max" to ensure proper functionality
        //Create the initialization configuration
        val initConfig = AppLovinSdkInitializationConfiguration.builder(MAX_AD_SDK_KEY, this)
            .setMediationProvider(AppLovinMediationProvider.MAX).build()

        val settings = AppLovinSdk.getInstance(this).settings
        settings.userIdentifier = UserProfileHelper.getUserId().toString()
        settings.termsAndPrivacyPolicyFlowSettings.apply {
            isEnabled = true
            privacyPolicyUri = Uri.parse(HttpUrls.PRIVACY_URL)
            termsOfServiceUri = Uri.parse(HttpUrls.SERVICE_URL)
        }

        //Initialize the SDK with the configuration
        AppLovinSdk.getInstance(getContext()).initialize(initConfig) { config ->
            //Start loading ads
            Logger.d("AppLovinSdk加载成功.countryCode:${config.countryCode}")
        }

        //MAX广告调试
        //AppLovinSdk.getInstance(this).showMediationDebugger()

        //初始化 Google 移动广告 SDK
        /*CoroutineScope(Dispatchers.IO).launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@HomeActivity) { status ->
                status.adapterStatusMap.forEach { (t, u) ->
                    Logger.d("MobileAds加载成功:$t initializationState:${u.initializationState} description:${u.description}")
                }
            }
        }*/

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 20)  // 设置10秒后触发通知

        /* createNotificationChannel(this)
         setAlarm(this, calendar)*/

        getDeepLinK()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "offline_notification_channel"
            val channelName = "Offline Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = "Channel for offline notifications"

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setAlarm(context: Context, triggerTime: Calendar) {
        //Alarm方式实现需要添加权限
        /*val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pendingIntent)*/

        //WorkManager方式实现
        //一次性任务
        val notificationWork = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInitialDelay(10, TimeUnit.SECONDS)  // 10秒后发送通知
            .build()
        WorkManager.getInstance(context).enqueue(notificationWork)

        //取消任务
        //WorkManager.getInstance(context).cancelWorkById(notificationWork.id)

        // 创建定期任务请求，设置为每天执行一次
        val dailyWorkRequest =
            PeriodicWorkRequest.Builder(NotificationWorker::class.java, 24, TimeUnit.HOURS).build()

        // 调度任务
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailySignInReminder", ExistingPeriodicWorkPolicy.KEEP, dailyWorkRequest
        )

        //WorkManager.getInstance(context).cancelWorkById(dailyWorkRequest.id)

    }

    private fun getAppVersion() {

        EasyHttp.get(this).api(AppVersionApi())
            .request(object : OnHttpListener<HttpData<AppVersionApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<AppVersionApi.Bean>?) {
                    result?.getData()?.apply {
                        if (updateStatus == 1) {
                            mAppVersionBean = this

                            appUpdateManager = AppUpdateManagerFactory.create(getContext())

                            // Returns an intent object that you use to check for an update.
                            val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

                            mUpdateType =
                                if (updateType == 0) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE

                            // Checks that the platform will allow the specified type of update.
                            appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
                                Logger.d("getAppVersion.appUpdateInfo: ${appUpdateInfo.updateAvailability()}")
                                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                    // This example applies an immediate update. To apply a flexible update
                                    // instead, pass in AppUpdateType.FLEXIBLE
                                    && appUpdateInfo.isUpdateTypeAllowed(mUpdateType)
                                ) {

                                    // Request the update.
                                    appUpdateManager?.startUpdateFlowForResult(
                                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                        appUpdateInfo,
                                        // an activity result launcher registered via registerForActivityResult
                                        activityResultLauncher,
                                        // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                                        // flexible updates.
                                        AppUpdateOptions.newBuilder(mUpdateType).build()
                                    )
                                }
                            }?.addOnFailureListener {
                                Logger.e("getAppVersion.Exception: ${it.message}")
                            }

                            appUpdateManager?.registerListener(listener)

                            /* */
                        }
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    private fun getDeepLinK() {

        /*EasyHttp.post(this).api(
            TiktokAccessTokenApi(
                "7383901465426788369",
                "a93753d8823f99e9b54cdf799e08c06a5d6aa924",
                "112641f295bd796f7fe137f6fca7652e9eb2997a"
            )
        ).request(object : OnHttpListener<HttpData<TiktokAccessTokenApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<TiktokAccessTokenApi.Bean>?) {
                }

                override fun onHttpFail(throwable: Throwable?) {
                }
            })*/

        /*EasyHttp.post(this).api(TikTokDeepLinkApi())
            .request(object : OnHttpListener<HttpData<TikTokDeepLinkApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<TikTokDeepLinkApi.Bean>?) {
                }

                override fun onHttpFail(throwable: Throwable?) {
                }
            })*/

    }

    private var activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            // handle callback
            if (result.resultCode != RESULT_OK) {
                Logger.d("getAppVersion.Update flow failed! Result code: " + result.resultCode)
                // If the update is canceled or fails,
                // you can request to start the update again.
                mAppVersionBean?.apply {
                    if (mUpdateType == AppUpdateType.IMMEDIATE) {
                        UpdateDialog.Builder(this@HomeActivity)
                            // 是否强制更新
                            .setForceUpdate(true).setPlayStoreUrl(url)
                            // 更新日志
                            .setUpdateLog(summary).show()
                    }
                }
            }
        }

    val listener = InstallStateUpdatedListener { state ->
        // (Optional) Provide a download progress bar.
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            // Show update progress bar.
        } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // 提示用户重新启动应用以完成更新
            showUpdateCompletedSnackbar()
        }
    }

    private fun showUpdateCompletedSnackbar() {
        val mainView: View? = findViewById(R.id.activity_main_layout)
        mainView?.let {
            Snackbar.make(
                it, getString(R.string.an_update), Snackbar.LENGTH_INDEFINITE
            ).apply {
                setAction(getString(R.string.restart)) {
                    appUpdateManager?.completeUpdate()
                    appUpdateManager?.unregisterListener(listener)
                }
                val textView =
                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                textView.setTextColor(Color.WHITE)
                setActionTextColor(ContextCompat.getColor(context, R.color.color_C640FF))
                show()
            }
        }
    }

    private fun getThirdLoginIntegral() {
        EasyHttp.get(this).api(TaskListApi())
            .request(object : OnHttpListener<HttpData<List<TaskListApi.Bean>>> {
                override fun onHttpSuccess(result: HttpData<List<TaskListApi.Bean>>?) {
                    result?.getData()?.onEach {
                        if (it.businessType == 7) {
                            thirdLoginIntegral = it.task?.integral
                            return@onEach
                        }
                    }
                }

                override fun onHttpFail(p0: Throwable?) {}
            })
    }

    private fun appStatusReport(type: Int) {
        EasyHttp.post(this).api(AppStatusReportApi(type = type))
            .request(object : OnHttpListener<HttpData<Void>> {
                override fun onHttpSuccess(result: HttpData<Void>?) {}

                override fun onHttpFail(t: Throwable?) {}
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: LoginAgainNotify) {
        if (notify.getUserInfo) {
            (pagerAdapter?.getItem(0) as HomeFragment).getHomeList(true)
            (pagerAdapter?.getItem(1) as MyListFragment).getMovieList()
            (pagerAdapter?.getItem(2) as BonusFragment).getBonusTaskList(false)
            (pagerAdapter?.getItem(3) as MeFragment).setUserInfo()
        } else {
            (pagerAdapter?.getItem(0) as HomeFragment).getHomeDataFailed(true)
            (pagerAdapter?.getItem(1) as MyListFragment).setEmptyDataView()
            (pagerAdapter?.getItem(2) as BonusFragment).setEmptyDataView()
            (pagerAdapter?.getItem(3) as MeFragment).setDefaultUserInfo()
        }
    }

    private var isScrolledToTop = true

    fun setHomeIcon(isScrolledToTop: Boolean) {
        if (this.isScrolledToTop == isScrolledToTop) return

        navigationAdapter?.getData()?.set(
            0, NavigationAdapter.MenuItem(
                getString(if (isScrolledToTop) R.string.home_nav_index else R.string.home_nav_index_top),
                ContextCompat.getDrawable(
                    this,
                    if (isScrolledToTop) R.drawable.home_home_selector else R.drawable.home_home_scroll_selector
                ),
                isScrolledToTop
            )
        )
        this.isScrolledToTop = isScrolledToTop

        val translationY = ConvertUtils.dp2px(22f)
        val endTranslationY = (if (isScrolledToTop) translationY else -translationY).toFloat()

        val translationYOut = ObjectAnimator.ofFloat(
            navigationView?.get(0)?.findViewById(R.id.iv_home_navigation_icon),
            "translationY",
            0f,
            endTranslationY
        )
        homeIconTranslationTime = System.currentTimeMillis()
        translationYOut.setDuration(150)
        translationYOut.start()

        postDelayed({ navigationAdapterNotify() }, 150)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun navigationAdapterNotify(hasAnim: Boolean = true) {
        if (hasAnim.not()) navigationAdapter?.getItem(0)?.isScrollingUp = null
        //只有首页上下滚动才出发home Tab图标动画 这里处理第一段动画 navigationAdapter里处理第二段动画
        navigationAdapter?.notifyDataSetChanged()
    }

    /**检查剪贴板中是否有数据*/
    private fun getClipboardText() {/*val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboardManager.hasPrimaryClip()) {
            // 获取剪贴板的数据项
            val clipData = clipboardManager.primaryClip
            // 获取剪贴板中最新的数据项
            val item = clipData?.getItemAt(0)

            Logger.e("粘贴板数据-->${item?.text}")
            if (item?.text.toString() == clipboardText) return

            // 获取剪贴板中最新的文本数据
            item?.text?.toString()?.let {
                if (it.isNotEmpty() && it.contains("dramatime:playvideo")) {
                    clipboardText = it

                    if (MMKVExt.getDurableMMKV()?.decodeBool(KEY_AD_COMPLETION_REGISTER) == true) {
                        (pagerAdapter?.getItem(0) as HomeFragment).getMainMovie(clipboardText)
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("text", ""))
                    }
                }
            }
        }*/
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        pagerAdapter?.let {
            switchFragment(it.getFragmentIndex(getSerializable(INTENT_KEY_IN_FRAGMENT_CLASS)))
        }

        intent?.let { handleNotificationClick(it) }
    }

    private fun handleNotificationClick(intent: Intent) {
        // 获取 Firebase 推送消息中的数据
        val extras = intent.extras
        if (extras != null) {
            // 处理推送消息数据
            val messageId = extras.getString("messageId")
            val path = extras.getString("path")
            val query = extras.getString("query").toString()
            reportClickMessage(messageId)
            Logger.e("handleNotificationClick.extras:$extras")
            if (path?.isNotEmpty() == true) {
                NoticeHandler.parseNotice(this, path, query)
                if (path == "/gift_card") {
                    val mLimitDialogNoticeBean = GsonFactory.getSingletonGson().fromJson(
                        query, LimitDialogNoticeBean::class.java
                    )
                    //应用已经启动点击折扣推送也要展示礼物弹窗，用通知的方式去触发
                    if ((pagerAdapter?.getItem(0) as HomeFragment).isLoading()) {
                        EventBus.getDefault()
                            .post(ShowLimitDialogNotify(0, mLimitDialogNoticeBean.idList, 7))
                    } else {
                        idList = mLimitDialogNoticeBean.idList
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewPager?.let {
            // 保存当前 Fragment 索引位置
            outState.putInt(INTENT_KEY_IN_FRAGMENT_INDEX, it.currentItem)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // 恢复当前 Fragment 索引位置
        switchFragment(savedInstanceState.getInt(INTENT_KEY_IN_FRAGMENT_INDEX))
    }

    fun switchFragment(fragmentIndex: Int) {
        if (fragmentIndex == -1) {
            return
        }
        when (fragmentIndex) {
            0, 1, 2, 3 -> {
                viewPager?.currentItem = fragmentIndex
                navigationAdapter?.setSelectedPosition(fragmentIndex)
            }
        }
    }

    /**
     * [NavigationAdapter.OnNavigationListener]
     */
    override fun onNavigationItemSelected(position: Int): Boolean {
        return when (position) {
            0, 1, 2, 3 -> {
                viewPager?.currentItem = position

                //如果首页图标动画还没结束就切换了tab 则把首页icon恢复位置
                if ((System.currentTimeMillis() - homeIconTranslationTime) < 150) {
                    val translationY = ConvertUtils.dp2px(22f)
                    val startTranslationY = (translationY).toFloat()

                    val translationYIn = ObjectAnimator.ofFloat(
                        navigationView?.get(0)?.findViewById(R.id.iv_home_navigation_icon),
                        "translationY",
                        startTranslationY,
                        0f
                    )
                    translationYIn.setDuration(150)
                    translationYIn.start()
                }
                true
            }

            else -> false
        }
    }

    override fun onIdenticalSelected(position: Int) {
        if (position == 0 && isScrolledToTop.not()) (pagerAdapter?.getItem(0) as HomeFragment).smoothScrollToTop()
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig() // 指定导航栏背景颜色
            .navigationBarColor(R.color.white)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            moveTaskToBack(true)
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        ActivityManager.getInstance().unregisterApplicationLifecycleCallback(this)
        super.onDestroy()
        viewPager?.adapter = null
        navigationView?.adapter = null
        navigationAdapter?.setOnNavigationListener(null)
        //结束连接
        GoogleBillingManager.endConn()
    }

    override fun onApplicationCreate(activity: Activity) {}

    override fun onApplicationDestroy(activity: Activity) {}

    override fun onApplicationBackground(activity: Activity) {
        appStatusReport(0)
    }

    override fun onApplicationForeground(activity: Activity) {
        if (pushEnable != null && pushEnable != NotificationUtils.areNotificationsEnabled()) {
            pushEnable = NotificationUtils.areNotificationsEnabled()
            (pagerAdapter?.getItem(0) as HomeFragment).setPushToken()
        }
        appStatusReport(1)
    }

    fun refreshNotificationsEnabledStatus() {
        (pagerAdapter?.getItem(2) as BonusFragment).refreshNotificationsEnabledStatus()
    }
}