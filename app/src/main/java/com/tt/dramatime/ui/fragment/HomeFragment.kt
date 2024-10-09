package com.tt.dramatime.ui.fragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.BarUtils.getStatusBarHeight
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.NotificationUtils
import com.blankj.utilcode.util.ScreenUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tt.base.BaseDialog
import com.tt.dramatime.R
import com.tt.dramatime.app.AppConstant
import com.tt.dramatime.app.AppConstant.UUID_LOGIN
import com.tt.dramatime.app.BaseViewBindTitleBarFragment
import com.tt.dramatime.databinding.HomeFragmentBinding
import com.tt.dramatime.http.api.AdCompleteRegisterApi
import com.tt.dramatime.http.api.BannerApi
import com.tt.dramatime.http.api.CommonAdConfigApi
import com.tt.dramatime.http.api.CommonConfigApi
import com.tt.dramatime.http.api.DiscountPopupApi
import com.tt.dramatime.http.api.HomeListApi
import com.tt.dramatime.http.api.HomeListApi.Bean.MovieListBean
import com.tt.dramatime.http.api.MainMovieApi
import com.tt.dramatime.http.api.MovieByCodeApi
import com.tt.dramatime.http.api.MovieRemindApi
import com.tt.dramatime.http.api.RechargeApi
import com.tt.dramatime.http.api.SetPushApi
import com.tt.dramatime.http.api.UpdateTokenApi
import com.tt.dramatime.http.api.UserInfoApi
import com.tt.dramatime.http.api.UuidLoginApi
import com.tt.dramatime.http.bean.VideoModel
import com.tt.dramatime.http.db.CommonAdConfigHelper
import com.tt.dramatime.http.db.CommonConfigHelper
import com.tt.dramatime.http.db.MMKVDurableConstant
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_AD_COMPLETION_REGISTER
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_ALREADY_OPEN
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_FIRST_PLAY
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_INSTALL_REFERRER
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.MMKVUserConstant
import com.tt.dramatime.http.db.MMKVUserConstant.Companion.KEY_AUTHORIZATION
import com.tt.dramatime.http.db.MyListHelper
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.ActivityManager
import com.tt.dramatime.manager.DialogManager
import com.tt.dramatime.other.AppConfig
import com.tt.dramatime.other.PermissionCallback
import com.tt.dramatime.ui.activity.HomeActivity
import com.tt.dramatime.ui.activity.home.MoreRankActivity
import com.tt.dramatime.ui.activity.home.SearchActivity
import com.tt.dramatime.ui.activity.home.VideoCatalogActivity
import com.tt.dramatime.ui.activity.player.BonusActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.adapter.home.EpisodesAdapter
import com.tt.dramatime.ui.adapter.home.EpisodesDetailedAdapter
import com.tt.dramatime.ui.adapter.home.EpisodesRankAdapter
import com.tt.dramatime.ui.adapter.home.FirstDramaAdapter
import com.tt.dramatime.ui.adapter.home.HomeHeadAdapter
import com.tt.dramatime.ui.adapter.home.HomeListAdapter
import com.tt.dramatime.ui.dialog.limit.EpDiscountsDialog
import com.tt.dramatime.ui.dialog.limit.LimitDiscountsDialog
import com.tt.dramatime.ui.dialog.limit.LimitOneDiscountsDialog
import com.tt.dramatime.ui.dialog.limit.VipDiscountsDialog
import com.tt.dramatime.ui.dialog.player.EnableNotificationsDialog
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.util.eventbus.LoginFailedNotify
import com.tt.dramatime.util.eventbus.MovieRemindNotify
import com.tt.dramatime.util.eventbus.PaySuccessNotify
import com.tt.dramatime.util.eventbus.PushTokenNotify
import com.tt.dramatime.util.eventbus.ShowLimitDialogNotify
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Random


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 首页Fragment
 * </pre>
 */
class HomeFragment :
    BaseViewBindTitleBarFragment<HomeFragmentBinding, HomeActivity>({ HomeFragmentBinding.inflate(it) }),
    OnRefreshListener, OnLoadMoreListener {

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun isStatusBarEnabled(): Boolean {
        // 使用沉浸式状态栏
        return !super.isStatusBarEnabled()
    }

    private val mHomeListAdapter by lazy(LazyThreadSafetyMode.NONE) {
        HomeListAdapter(mutableListOf())
    }
    private val mHomeHeadAdapter by lazy(LazyThreadSafetyMode.NONE) {
        HomeHeadAdapter(viewLifecycleOwner)
    }

    private var mFirstDramaAdapter: FirstDramaAdapter? = null

    private var isVisibleHistory = false
    private var registrationToken: String? = null
    private var pageNum = 1
    private var userId = 0

    private var adReportFailedCount = 0
    private var getMainMovieFailedCount = 0

    private var isFirstPlay = false

    private lateinit var helper: QuickAdapterHelper
    private var adapterList: MutableList<BaseQuickAdapter<*, *>>? = null
    private var isAlreadyOpen = false
    private var isShowLimitDialog = false

    private var isUpdateToken = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun initView() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        binding.homeLl.setPadding(0, getStatusBarHeight() + ConvertUtils.dp2px(61f), 0, 0)
        helper = QuickAdapterHelper.Builder(mHomeListAdapter).build()
        binding.homeRv.adapter = helper.adapter
        binding.refreshSl.setOnRefreshListener(this)
        binding.refreshSl.setOnLoadMoreListener(this)

        mHomeListAdapter.addOnItemChildClickListener(R.id.more_tv) { _, _, position ->
            mHomeListAdapter.getItem(position)?.apply {
                title?.let { title ->
                    code?.let { code ->
                        if (blockStyle == "5") {
                            MoreRankActivity.start(requireContext(), title, code)
                        } else {
                            VideoCatalogActivity.start(
                                requireContext(), mHomeListAdapter.items.toMutableList(), code
                            )
                        }
                    }
                }
            }
        }

        val titleBarBg = binding.titleBar.background
        titleBarBg.alpha = 0

        binding.homeNsv.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY + 20) {
                binding.historyCl.visibility = View.GONE
            }
            if (scrollY < oldScrollY - 20 && isVisibleHistory) {
                binding.historyCl.visibility = View.VISIBLE
            }

            titleBarBg.alpha = if (scrollY > 0) 255 else 0

            getAttachActivity()?.setHomeIcon(scrollY < ScreenUtils.getScreenHeight() / 2)
        })

        if (MMKVExt.getDurableMMKV()?.decodeBool(KEY_ALREADY_OPEN) == true) {
            isAlreadyOpen = true
            if (NotificationUtils.areNotificationsEnabled().not() && timeInterval()) {
                DialogManager.getInstance(this)
                    .addShow(EnableNotificationsDialog.Builder(requireContext(), this).create())
                MMKVExt.getDurableMMKV()
                    ?.encode(MMKVDurableConstant.KEY_NOTICE_SHOW_TIME, System.currentTimeMillis())
            }
        }
        //切换语言需要切回主页
        getAttachActivity()?.switchFragment(0)

        setOnClickListener(
            binding.errorLl.operationBtn, binding.historyCl, binding.bonusLav, binding.searchIv
        )

    }

    override fun initData() {
        if (EventBus.getDefault().isRegistered(this).not()) {
            EventBus.getDefault().register(this)
        }

        //loginStatus为Null为推送进来的
        if (getAttachActivity()?.loginStatus == null || getAttachActivity()?.loginStatus == 0) {
            if (MMKVExt.getUserMmkv()?.getString(KEY_AUTHORIZATION, "")
                    .isNullOrEmpty() || MMKVExt.getDurableMMKV()
                    ?.decodeBool(KEY_ALREADY_OPEN) == false
            ) {
                uuidLogin(true)
            } else {
                // 刷新用户信息
                updateToken(isPush = true)
            }
        } else if (getAttachActivity()?.loginStatus == 2) {
            binding.loadingFl.visibility = View.GONE
            binding.errorLl.rootError.visibility = View.VISIBLE
        } else {
            getUserInfo()
            getRecharge()
            getCommonConfig()
            getCommonAdConfig()
            getPushToken()
        }

        MMKVExt.getDurableMMKV()?.encode(KEY_ALREADY_OPEN, true)
    }

    @SingleClick(500)
    override fun onClick(view: View) {
        when (view) {
            binding.errorLl.operationBtn -> {
                showDialog()
                val authorization = MMKVExt.getUserMmkv()?.getString(KEY_AUTHORIZATION, "")
                if (getAttachActivity()?.loginStatus == 2) {
                    if (authorization?.isEmpty() == true) {
                        uuidLogin()
                    } else {
                        updateToken()
                    }
                } else {
                    getUserInfo()
                }
            }

            binding.historyCl -> {
                if (MyListHelper.myList.isNotEmpty()) {
                    MyListHelper.myList[0].movieId?.let { movieId ->
                        PlayerActivity.start(context, movieId)
                    }
                }
            }

            binding.bonusLav -> {
                firebaseAnalytics.logEvent("home_bonus", null)
                startActivity(BonusActivity::class.java)
            }

            binding.searchIv -> {
                firebaseAnalytics.logEvent("drama_search", null)
                startActivity(SearchActivity::class.java)
            }
        }
    }

    fun showCustomNotification(context: Context) {

        val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
        var channelId = ""

        val mMessageTitle = "messageTitle" ?: "DramaTime"
        val mMessageBody = "messageBody" ?: "DramaTime"
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

        // 创建自定义通知布局
        val customView = RemoteViews(context.packageName, R.layout.custom_notification)
        customView.setTextViewText(R.id.notification_title, "自定义通知标题")
        customView.setTextViewText(R.id.notification_text, "自定义通知内容")

        // 创建展开状态的 RemoteViews
        val expandedView = RemoteViews(context.packageName, R.layout.custom_notification_expanded)

        // 设置按钮点击事件的意图
        val intent = Intent(context, BonusActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= 34) PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_UPDATE_CURRENT
            else PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        //expandedView.setOnClickPendingIntent(R.id.notification_button, pendingIntent)


        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(requireContext(), channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setCustomContentView(customView) // 设置自定义布局
                .setCustomBigContentView(expandedView) // 展开状态布局
                // 设置通知小图标
                .setSmallIcon(R.drawable.app_logo)
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
                    pendingIntent
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

    override fun onResume() {
        super.onResume()
        setViewingHistory()
        if (isFirstPlay) {
            initPushToken()
            isFirstPlay = false
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        getUserInfo()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        loadHomeList()
    }

    fun smoothScrollToTop() {
        if (binding.refreshSl.isLoading) return
        binding.homeNsv.smoothScrollTo(0, 0)
    }

    fun getHomeList(reset: Boolean = false) {
        if (reset) {
            binding.loadingFl.visibility = View.VISIBLE
            mHomeListAdapter.submitList(mutableListOf())
            helper.removeAdapter(mHomeHeadAdapter)
        }

        binding.refreshSl.resetNoMoreData()
        pageNum = 1

        EasyHttp.get(this@HomeFragment).api(BannerApi())
            .request(object : OnHttpListener<HttpData<List<BannerApi.Bean>>?> {
                override fun onHttpSuccess(bannerResult: HttpData<List<BannerApi.Bean>>?) {
                    EasyHttp.get(this@HomeFragment).api(HomeListApi(pageNum = pageNum))
                        .request(object : OnHttpListener<HttpData<List<HomeListApi.Bean>>?> {
                            override fun onHttpSuccess(homeListResult: HttpData<List<HomeListApi.Bean>>?) {
                                setHomeData(bannerResult, homeListResult)
                            }

                            override fun onHttpFail(throwable: Throwable?) {
                                toast(throwable?.message)
                                report()
                                getHomeDataFailed(reset)
                            }
                        })
                }

                override fun onHttpFail(throwable: Throwable?) {
                    toast(throwable?.message)
                    report()
                    getHomeDataFailed(reset)
                }
            })
    }

    private fun setHomeData(
        bannerResult: HttpData<List<BannerApi.Bean>>?,
        homeListResult: HttpData<List<HomeListApi.Bean>>?
    ) {
        if (binding.errorLl.rootError.visibility == View.VISIBLE) {
            binding.errorLl.rootError.visibility = View.GONE
        }

        binding.refreshSl.finishRefresh()

        if (bannerResult?.getData()?.isNotEmpty() == true) {
            helper.addBeforeAdapter(0, mHomeHeadAdapter)
            // 在这里进行 UI 刷新
            mHomeHeadAdapter.item = bannerResult.getData()
        }

        val list = homeListResult?.getData()?.filter { it.code != "recommend_movie" }
        adapterList = mutableListOf()
        list?.forEach { bean ->
            bean.movieList?.let { movieList ->
                when (bean.blockStyle) {
                    "2" -> adapterList?.add(EpisodesDetailedAdapter(movieList))

                    "5" -> adapterList?.add(EpisodesRankAdapter(movieList))

                    "3", "6" -> adapterList?.add(
                        EpisodesAdapter(
                            movieList,
                            linear = false,
                            pageStatus = false,
                            moreDataList = list as MutableList<HomeListApi.Bean>?,
                            code = bean.blockStyle
                        )
                    )

                    "7" -> {
                        mFirstDramaAdapter = FirstDramaAdapter(movieList)
                        mFirstDramaAdapter?.addOnDebouncedChildClick(
                            R.id.reserve_ll, 1000
                        ) { adapter, _, position ->
                            movieRemind(adapter, position)
                        }
                        mFirstDramaAdapter?.animationEnable = false
                        adapterList?.add(mFirstDramaAdapter!!)
                    }

                    else -> adapterList?.add(
                        EpisodesAdapter(
                            movieList,
                            true,
                            bean.pageStatus == true && bean.blockStyle == "1",
                            list as MutableList<HomeListApi.Bean>?,
                            bean.code
                        )
                    )
                }
            }
        }

        mHomeListAdapter.animationEnable = false
        mHomeListAdapter.setAdapterList(adapterList!!)
        mHomeListAdapter.submitList(list)

        if (binding.loadingFl.visibility == View.VISIBLE) {
            binding.loadingFl.visibility = View.GONE
        }

        hideDialog()

        report()

        getDiscountPopup(2000, idList = getAttachActivity()?.idList, source = 5)

        pageNum++
    }

    fun getHomeDataFailed(reset: Boolean = false) {
        binding.refreshSl.finishRefresh()
        binding.loadingFl.visibility = View.GONE
        if (mHomeListAdapter.items.isEmpty() || reset) {
            if (reset) {
                mHomeListAdapter.submitList(mutableListOf())
                helper.removeAdapter(mHomeHeadAdapter)
            }
            binding.errorLl.rootError.visibility = View.VISIBLE
        }
        hideDialog()
    }

    private fun report() {
        val referrer = MMKVExt.getDurableMMKV()?.decodeString(KEY_INSTALL_REFERRER) ?: ""
        val clipboardText = getAttachActivity()?.clipboardText
        if (MMKVExt.getDurableMMKV()?.decodeString(KEY_AD_COMPLETION_REGISTER) != referrer) {
            adCompleteRegister(referrer, clipboardText)
            //0代表没播放过剧 则获取主推剧
            if (MMKVExt.getDurableMMKV()?.decodeInt(KEY_FIRST_PLAY) == 0) {
                getMainMovie(referrer)
            }
        }
    }

    private fun initPushToken() {
        XXPermissions.with(this).permission(Permission.POST_NOTIFICATIONS)
            .request(object : PermissionCallback() {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    getPushToken()
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    getPushToken()
                }
            })
    }

    /**Firebase推送初始化获取token*/
    private fun getPushToken() {
        if (registrationToken == null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Logger.w("Fetching FCM registration token failed:${task.exception} userid:${UserProfileHelper.getUserId()}")
                    Firebase.crashlytics.recordException(Error("Fetching FCM registration token failed：${task.exception} userid:${UserProfileHelper.getUserId()}"))
                    setPushToken()
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                registrationToken = task.result
                Logger.d("Fetching FCM registration result.token==$registrationToken")
                setPushToken()
            }
        } else {
            setPushToken()
        }
    }

    fun setPushToken() {
        EasyHttp.post(this@HomeFragment).api(
            SetPushApi(registrationToken, NotificationUtils.areNotificationsEnabled())
        ).request(object : OnHttpListener<HttpData<Void>> {
            override fun onHttpSuccess(result: HttpData<Void>?) {
                getAttachActivity()?.refreshNotificationsEnabledStatus()
            }

            override fun onHttpFail(throwable: Throwable?) {}
        })
    }

    private fun adCompleteRegister(referrer: String, clipboardText: String?) {
        EasyHttp.post(this).api(
            AdCompleteRegisterApi(userId, referrer, clipboardText)
        ).request(object : OnHttpListener<HttpData<Void>> {
            override fun onHttpSuccess(result: HttpData<Void>?) {
                MMKVExt.getDurableMMKV()?.encode(KEY_AD_COMPLETION_REGISTER, referrer)
            }

            override fun onHttpFail(throwable: Throwable?) {
                if (adReportFailedCount > 3) return
                postDelayed({ adCompleteRegister(referrer, clipboardText) }, 2000)
                adReportFailedCount++
            }
        })
    }

    private fun getMainMovie(data: String) {
        val result = object : OnHttpListener<HttpData<VideoModel>> {
            override fun onHttpSuccess(result: HttpData<VideoModel>?) {
                result?.getData()?.apply {
                    isFirstPlay = true
                    PlayerActivity.start(context, movieId, 1, AppConstant.adLang)
                }
            }

            override fun onHttpFail(throwable: Throwable?) {
                if (getMainMovieFailedCount > 3) return
                postDelayed({ getMainMovie(data) }, 2000)
                getMainMovieFailedCount++
            }
        }

        val sourceCode = AppConstant.sourceCode

        if (data.contains("apps.facebook.com") || data.contains("dramatime") || sourceCode == null) {
            EasyHttp.get(this).api(MainMovieApi(data)).request(result)
        } else {
            EasyHttp.get(this).api(MovieByCodeApi(sourceCode)).request(result)
        }
    }

    private fun loadHomeList() {
        EasyHttp.get(this).api(
            HomeListApi(
                mHomeListAdapter.getItem(mHomeListAdapter.items.lastIndex)?.code, pageNum
            )
        ).request(object : OnHttpListener<HttpData<List<HomeListApi.Bean>>?> {
            override fun onHttpSuccess(result: HttpData<List<HomeListApi.Bean>>?) {
                binding.refreshSl.finishLoadMore()
                adapterList?.let {
                    val adapter = it.getOrNull(it.lastIndex)
                    result?.getData()?.let { data ->
                        if (data.isNotEmpty() && data[0].movieList.isNullOrEmpty().not()) {
                            adapter?.addAll(data[0].movieList as List<Nothing>)
                            pageNum++
                            data[0].pageNum = pageNum
                        } else {
                            binding.refreshSl.setNoMoreData(true)
                        }
                    } ?: binding.refreshSl.setNoMoreData(true)
                }
            }

            override fun onHttpFail(throwable: Throwable?) {
                toast(throwable?.message)
                binding.refreshSl.finishLoadMore()
            }
        })
    }

    private fun movieRemind(adapter: BaseQuickAdapter<MovieListBean, *>, position: Int) {
        adapter.items[position].movieId?.let {
            if (adapter.items[position].activity == null) {
                PlayerActivity.start(context, it)
                return
            }
            val type = if (adapter.items[position].activity?.remindStatus == 0) 1 else 0

            if (type == 1 && NotificationUtils.areNotificationsEnabled().not()) {
                EnableNotificationsDialog.Builder(requireContext(), this)
                    .addOnDismissListener(object : BaseDialog.OnDismissListener {
                        override fun onDismiss(dialog: BaseDialog?) {
                            if (NotificationUtils.areNotificationsEnabled()) {
                                movieRemind(adapter, position)
                            }
                        }
                    }).show()
                return
            }

            EasyHttp.post(this).api(MovieRemindApi(it, type))
                .request(object : OnHttpListener<HttpData<Void>> {
                    override fun onHttpSuccess(result: HttpData<Void>?) {
                        adapter.items[position].activity?.remindStatus = type
                        adapter.notifyItemChanged(position)
                        toast(if (type == 0) R.string.cancel_reservation else R.string.reservation_successful)
                    }

                    override fun onHttpFail(throwable: Throwable?) {
                        toast(throwable?.message)
                    }
                })
        }
    }

    fun getUserInfo() {
        EasyHttp.get(this).api(UserInfoApi())
            .request(object : OnHttpListener<HttpData<UserInfoApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UserInfoApi.Bean?>) {
                    data.getData()?.let {
                        getAttachActivity()?.loginStatus = 1
                        getAttachActivity()?.navigationAdapterNotify(false)
                        userId = it.userId
                        UserProfileHelper.setProfile(it)
                        getHomeList()
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    toast(throwable?.message)
                    getHomeDataFailed()
                }
            })
    }

    private fun getRecharge() {
        EasyHttp.get(this).api(RechargeApi())
            .request(object : OnHttpListener<HttpData<RechargeApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<RechargeApi.Bean>?) {}
                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    private fun getCommonConfig() {
        EasyHttp.get(this).api(CommonConfigApi())
            .request(object : OnHttpListener<HttpData<CommonConfigApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<CommonConfigApi.Bean>?) {
                    result?.getData()?.let {
                        CommonConfigHelper.setConfig(it)
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    private fun getCommonAdConfig(isPay: Boolean = false) {
        if (CommonAdConfigHelper.config.isEmpty() || isPay) {
            EasyHttp.get(this).api(CommonAdConfigApi())
                .request(object : OnHttpListener<HttpData<List<CommonAdConfigApi.Bean>>> {
                    override fun onHttpSuccess(result: HttpData<List<CommonAdConfigApi.Bean>>?) {
                        result?.getData()?.let {
                            CommonAdConfigHelper.setConfig(it)
                        }
                    }

                    override fun onHttpFail(throwable: Throwable?) {}
                })
        }
    }

    private fun getDiscountPopup(
        delayMillis: Long = 0,
        loc: Int = 0,
        isShow: Boolean = false,
        idList: List<String>? = null,
        source: Int = 0
    ) {
        if (isShowLimitDialog || isAlreadyOpen.not()) return
        EasyHttp.get(this).api(DiscountPopupApi(loc, idList))
            .request(object : OnHttpListener<HttpData<DiscountPopupApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<DiscountPopupApi.Bean>?) {
                    isShowLimitDialog = true
                    val activity = ActivityManager.getInstance().getTopActivity()
                    postDelayed({
                        if (isAdded && activity == getAttachActivity() || isShow) {
                            result?.getData()?.apply {
                                val bean = this
                                var dialog: BaseDialog? = null
                                activity?.let { activity ->
                                    goods?.apply {
                                        recharge?.let {
                                            if (it.size == 1) {
                                                dialog = LimitOneDiscountsDialog.Builder(
                                                    activity, bean, it, source
                                                ).create()
                                            } else if (it.size > 1) {
                                                dialog = LimitDiscountsDialog.Builder(
                                                    activity, bean, it, source
                                                ).create()
                                            }
                                        }

                                        unlock?.let {
                                            dialog = EpDiscountsDialog.Builder(
                                                activity, bean, it, source
                                            ).create()
                                        }

                                        subscribe?.let {
                                            dialog =
                                                VipDiscountsDialog.Builder(activity, id, it, source)
                                                    .create()
                                        }

                                        dialog?.let {
                                            DialogManager.getInstance(this@HomeFragment).addShow(it)
                                        }
                                    }
                                }
                            }
                        }
                    }, delayMillis)
                }

                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    private fun setViewingHistory() {
        val myList = MyListHelper.myList

        if (myList.isNotEmpty()) {
            isVisibleHistory = true
            binding.historyCl.visibility = View.VISIBLE
            myList[0].apply {
                context?.let { GlideUtils.loadImage(it, poster, binding.historyCoveIv) }
                binding.historyTitleTv.text = title
                binding.historyEpTv.text = getString(R.string.ep_current, watchEpisode)
            }
        } else binding.historyCl.visibility = View.GONE
    }

    /**接口提示登录失败重新登录*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: LoginFailedNotify) {
        if (notify.updateToken) updateToken(false) else uuidLogin()
    }

    /**推送Token更新*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: PushTokenNotify) {
        Logger.e("推送Token更新:${notify.token}")
        registrationToken = notify.token
        setPushToken()
    }

    /**显示折扣弹窗通知*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: ShowLimitDialogNotify) {
        isShowLimitDialog = false
        getDiscountPopup(0, notify.loc, true, notify.idList, notify.source)
    }

    /**首发剧提醒通知*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: MovieRemindNotify) {
        val mMovieList = mHomeListAdapter.items.filter { it.blockStyle == "7" }[0].movieList

        mMovieList?.forEachIndexed { index, movieListBean ->
            if (movieListBean.movieId == notify.movieId) {
                mMovieList[index].activity?.remindStatus = 1
                mFirstDramaAdapter?.notifyItemChanged(index)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: PaySuccessNotify) {
        getCommonAdConfig(true)
    }

    @SingleClick(2000)
    private fun uuidLogin(isPush: Boolean = false) {
        context?.let {
            EasyHttp.post(this).api(UuidLoginApi(AppConfig.getWidevineID(it)))
                .request(object : OnHttpListener<HttpData<UuidLoginApi.Bean?>> {
                    override fun onHttpSuccess(data: HttpData<UuidLoginApi.Bean?>) {
                        notifyAuthLanguage(data)
                        MMKVExt.getUserMmkv()?.encode(MMKVUserConstant.KEY_LOGIN_SOURCE, UUID_LOGIN)
                        getUserInfo()
                        if (isPush) {
                            getUserInfo()
                            getRecharge()
                            getCommonConfig()
                            getCommonAdConfig()
                            getPushToken()
                        }
                    }

                    override fun onHttpFail(throwable: Throwable?) {
                        toast(throwable?.message)
                        hideDialog()
                    }
                })
        }
    }

    @SingleClick(2000)
    private fun updateToken(refreshHome: Boolean = true, isPush: Boolean = false) {
        if (isUpdateToken) return
        isUpdateToken = true
        // 刷新用户信息
        EasyHttp.post(this).api(UpdateTokenApi())
            .request(object : OnHttpListener<HttpData<UuidLoginApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UuidLoginApi.Bean?>) {
                    isUpdateToken = false
                    notifyAuthLanguage(data)
                    if (refreshHome) {
                        getUserInfo()
                    }

                    if (isPush) {
                        getRecharge()
                        getCommonConfig()
                        getCommonAdConfig()
                        getPushToken()
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    isUpdateToken = false
                    toast(throwable?.message)
                    hideDialog()
                }
            })
    }

    private fun timeInterval(): Boolean {
        val mNoticeTime =
            MMKVExt.getDurableMMKV()?.decodeLong(MMKVDurableConstant.KEY_NOTICE_SHOW_TIME) ?: 0
        //展示过后24小时不出现通知弹窗
        return System.currentTimeMillis() - mNoticeTime > 1000 * 60 * 60 * 24
    }

    /**更新token与内容语言*/
    private fun notifyAuthLanguage(data: HttpData<UuidLoginApi.Bean?>) {
        val authorization = "Bearer ${data.getData()?.accessToken}"
        // 更新 Authorization
        MMKVExt.getUserMmkv()?.putString(MMKVUserConstant.KEY_AUTHORIZATION, authorization)
        data.getData()?.contentLanguage?.let { contentLanguage ->
            MMKVExt.getDurableMMKV()
                ?.encode(MMKVDurableConstant.KEY_CONTENT_LANGUAGE, contentLanguage)
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        binding.homeRv.adapter = null
        binding.refreshSl.setOnRefreshListener(null)
        binding.refreshSl.setOnLoadMoreListener(null)
        binding.homeNsv.removeCallbacks {}
        super.onDestroy()
    }

}