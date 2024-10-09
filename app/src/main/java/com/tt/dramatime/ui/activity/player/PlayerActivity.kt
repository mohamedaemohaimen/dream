package com.tt.dramatime.ui.activity.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.gyf.immersionbar.ImmersionBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.smart.adapter.SmartViewPager2Adapter
import com.smart.adapter.interf.OnLoadMoreListener
import com.smart.adapter.transformer.SmartTransformer
import com.tt.base.BaseDialog
import com.tt.dramatime.R
import com.tt.dramatime.app.AppConstant.MAX_AD_UNIT_ID
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.PlayerActivityBinding
import com.tt.dramatime.http.api.AdTriggerApi
import com.tt.dramatime.http.api.CommonGetConfigApi
import com.tt.dramatime.http.api.EpisodesDetailApi
import com.tt.dramatime.http.api.HttpUrls.Companion.AD_EP_DETAIL_URL
import com.tt.dramatime.http.api.HttpUrls.Companion.AD_MOVIE_DETAIL_URL
import com.tt.dramatime.http.api.HttpUrls.Companion.EP_DETAIL_URL
import com.tt.dramatime.http.api.HttpUrls.Companion.MOVIE_DETAIL_URL
import com.tt.dramatime.http.api.InterceptListApi
import com.tt.dramatime.http.api.MovieCollectApi
import com.tt.dramatime.http.api.MovieDetailApi
import com.tt.dramatime.http.api.MovieListApi
import com.tt.dramatime.http.api.WalletBalanceApi
import com.tt.dramatime.http.bean.EpisodesPlayBean
import com.tt.dramatime.http.bean.NumberEpisodesBean
import com.tt.dramatime.http.bean.VideoModel
import com.tt.dramatime.http.db.MMKVDurableConstant
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_INTERCEPT_EPISODES_SHOW_TIME
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_VIDEO_GUIDE
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.ActivityManager
import com.tt.dramatime.ui.activity.HomeActivity
import com.tt.dramatime.ui.dialog.player.FirstDramaDialog
import com.tt.dramatime.ui.fragment.player.InterceptionPlayerFragment
import com.tt.dramatime.ui.fragment.player.PlayerFragment
import com.tt.dramatime.util.TransScaleFormer
import com.tt.dramatime.util.eventbus.InterceptionPlayEndNotify
import com.tt.dramatime.util.eventbus.SelectEpisodesNotify
import com.tt.dramatime.util.eventbus.StopPlayNotify
import com.tt.dramatime.util.eventbus.UnlockNotify
import com.tt.dramatime.util.eventbus.WatchingAdSuccessNotify
import com.tt.dramatime.util.eventbus.WatchingNotify
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit
import kotlin.math.pow


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 播放器
 * </pre>
 */
class PlayerActivity :
    BaseViewBindingActivity<PlayerActivityBinding>({ PlayerActivityBinding.inflate(it) }),
    MaxRewardedAdListener {

    companion object {
        const val KEY_MOVIE_ID = "key.movie.id"
        const val KEY_CURRENT_EPISODE = "key.current.episode"
        const val KEY_LANG = "key.lang"
        const val NOT_UNLOCKED = 0
        const val UNLOCKED = 1
        const val PRE_UNLOCK = 2
        const val INSUFFICIENT_COINS = 3

        fun start(
            context: Context?, movieId: String, currentEpisode: Int? = null, lang: String? = null
        ) {
            val intent = Intent(context, PlayerActivity::class.java)
            val bundle = Bundle()
            intent.putExtra(KEY_MOVIE_ID, movieId)
            intent.putExtra(KEY_CURRENT_EPISODE, currentEpisode)
            intent.putExtra(KEY_LANG, lang)
            intent.putExtras(bundle)

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context?.startActivity(intent)
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig().navigationBarColor(R.color.black)
    }

    override fun isStatusBarDarkFont(): Boolean {
        return !super.isStatusBarDarkFont()
    }

    private var movieId = ""
    private var movieCode = ""
    var lang: String? = null

    private var traceId = ""
    var watchAdCount = 0

    private var currentPage = 0

    var mCurrentEpisodePosition = 0
    var mCurrentEpisode = 1
    var lastEpisode: Int? = null
    var loadMovieDataStatus = false
    var loadNextStatus = true
    var collectStatus = false

    var mMovieDetailBean: MovieDetailApi.Bean? = null
    val mNumberEpisodesList = arrayListOf<NumberEpisodesBean>()
    val mEpisodesPlayList = arrayListOf<EpisodesPlayBean>()
    var mInterceptionVideoList = arrayListOf<VideoModel>()
    private var loadInterceptionVideoList = false

    private var rewardedAd: MaxRewardedAd? = null
    private var retryAttempt = 0.0
    private var adIsReady: Boolean? = null

    private var isLastPageSwiped = false
    private var isLastPageUnLocked = false

    /**开始剧集默认为1 如果是首发剧 会设置成0*/
    private var startEpisode = 1
    private var totalEpisode = 0
    var actualTotalEpisode = 0

    var isVip = false
    var adEnableStatus: Boolean? = false

    private var isAutoPlay = false

    private var mFirstDramaDialog: FirstDramaDialog.Builder? = null

    private val mAdapter by lazy(LazyThreadSafetyMode.NONE) {
        SmartViewPager2Adapter.Builder<VideoModel>(this).setOffscreenPageLimit(1).setPreLoadLimit(0)
            .addFragment(0, PlayerFragment::class.java).build(binding.playerVp)
    }

    private var mInterceptionAdapter: SmartViewPager2Adapter<VideoModel>? = null

    private val mOnPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            Logger.e("onPageSelected.position$position")
            if (mAdapter.getData().isEmpty()) return
            // 重置状态
            isLastPageSwiped = false
            isLastPageUnLocked = false

            currentPage = position

            mEpisodesPlayList.forEachIndexed { index, episodesPlayBean ->
                episodesPlayBean.isPlay = currentPage == index
            }

            //关闭引导动画
            if (mCurrentEpisodePosition != mAdapter.getItem(currentPage).ep - startEpisode && binding.guideLl.visibility == View.VISIBLE) {
                setGuideVisibility(false)
            }

            mCurrentEpisodePosition = mAdapter.getItem(currentPage).ep - startEpisode
            mCurrentEpisode = mAdapter.getItem(currentPage).ep

            //更新观看历史
            //mMovieDetailHelper.updateWatchHistory(mAdapter.getItem(currentPage).ep)

            mMovieDetailBean?.let {
                //观看上报 加载下一集会再次走这个回调 所以上一集等于当前集不上报
                mAdapter.getItem(currentPage).apply {
                    if (ep != lastEpisode) {
                        collect(ep, lastEpisode)
                    }
                    val unlockStatus =
                        unlockStatus == NOT_UNLOCKED || unlockStatus == INSUFFICIENT_COINS
                    //广告解锁开关开启并且该剧集未解锁
                    if (it.s2s?.enableStatus == true && unlockStatus) {
                        //每次滑动都重置观看广告次数
                        watchAdCount = 0
                        traceId = ""
                        adTrigger(true)
                    }
                }
            }
            lastEpisode = mAdapter.getItem(currentPage).ep
        }

        override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
            //停止滚动并且是滑动到最后一条时触发 优先判断是否解锁
            if (offsetPixels == 0 && position == mAdapter.itemCount - 1) {
                if (isLastPageUnLocked) {
                    toast(getString(R.string.unlock_hint))
                    isLastPageUnLocked = false
                } else if (isLastPageSwiped) {
                    if (mInterceptionVideoList.isNotEmpty()) {
                        backInterception(true)
                        EventBus.getDefault().post(StopPlayNotify())
                    } else toast(R.string.last_episode_hint)
                    isLastPageSwiped = false
                } else if (collectStatus && loadNextStatus.not()) {
                    loadNextEp()
                } else if (collectStatus.not()) {
                    collect(mAdapter.getItem(currentPage).ep, lastEpisode)
                }

                // 当前是最后一个页面，并且正在向右滑动
                val unlockStatus = mAdapter.getItem(currentPage).unlockStatus
                if (unlockStatus == NOT_UNLOCKED || unlockStatus == INSUFFICIENT_COINS) {
                    isLastPageUnLocked = true
                } else if (position == totalEpisode - startEpisode) {
                    isLastPageSwiped = true
                }
            }
            super.onPageScrolled(position, offset, offsetPixels)
        }
    }

    private val mOnPageChangeCallback2 = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            //自动播放的情况下 5s内有操作不自动进入拦截剧
            if (mInterceptionVideoList.size > 0) {
                if (autoPlayInterception && (position % mInterceptionVideoList.size) != 0) {
                    removeCallbacks(autoPlayRunnable)
                    autoPlayInterception = false
                }
            }
        }
    }

    private var autoPlayInterception = false
    private val autoPlayRunnable = Runnable { playCurrentMovie() }

    override fun initView() {
        LogUtils.dTag("PlayerActivity", "initView")
        if (EventBus.getDefault().isRegistered(this).not()) {
            EventBus.getDefault().register(this)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SECURE)

        binding.playerVp.adapter = mAdapter
        binding.playerVp.registerOnPageChangeCallback(mOnPageChangeCallback)
        binding.interceptionDramaVp.registerOnPageChangeCallback(mOnPageChangeCallback2)

        setOnClickListener(binding.errorLl.operationBtn, binding.backBtn, binding.playTv)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backInterception()
                EventBus.getDefault().post(StopPlayNotify())
            }
        })

        createRewardedAd()
    }

    override fun initData() {
        movieId = getString(KEY_MOVIE_ID)!!
        lang = getString(KEY_LANG)

        if (MMKVExt.getDurableMMKV()?.decodeInt(MMKVDurableConstant.KEY_FIRST_PLAY) != 1) {
            MMKVExt.getDurableMMKV()?.encode(MMKVDurableConstant.KEY_FIRST_PLAY, 1)

            /*postDelayed({
                if (isFinishing.not() && NotificationUtils.areNotificationsEnabled().not()) {
                    NotificationEnableDialogFragment().show(
                        supportFragmentManager, NotificationEnableDialogFragment.TAG
                    )
                }
            }, 30 * 1000)*/
        }

        getVideoData()
        getConfig()
    }

    @SingleClick(1000)
    override fun onClick(view: View) {
        when (view) {
            binding.backBtn -> onBackPressedDispatcher.onBackPressed()
            binding.errorLl.operationBtn -> if (loadMovieDataStatus.not()) getVideoData()
            binding.playTv -> playCurrentMovie()
        }
    }

    fun playCurrentMovie() {
        if (mInterceptionVideoList.size > 0) {
            val page = binding.interceptionDramaVp.currentItem % mInterceptionVideoList.size
            if (page >= 0) removeData(mInterceptionVideoList[page].movieId)
        }
    }

    private fun getInterceptList() {
        EasyHttp.get(this).api(InterceptListApi())
            .request(object : OnHttpListener<HttpData<ArrayList<VideoModel>>> {
                override fun onHttpSuccess(result: HttpData<ArrayList<VideoModel>>?) {
                    result?.getData()?.let {
                        mInterceptionVideoList = it
                        loadInterceptionVideoList = true
                        val adapter =
                            SmartViewPager2Adapter.Builder<VideoModel>(this@PlayerActivity)
                                .overScrollNever().setOffscreenPageLimit(1)
                                .asGallery(ConvertUtils.dp2px(49f), ConvertUtils.dp2px(49f))
                                .addFragment(0, InterceptionPlayerFragment::class.java)

                        if (mInterceptionVideoList.size > 2) {
                            adapter.setInfinite()
                            binding.interceptionDramaVp.setPageTransformer(TransScaleFormer())
                        } else {
                            adapter.setPagerTransformer(SmartTransformer.TRANSFORMER_ALPHA_SCALE)
                        }

                        mInterceptionAdapter = adapter.build(binding.interceptionDramaVp)
                            .addData(mInterceptionVideoList)
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    private fun getConfig() {
        EasyHttp.get(this).api(CommonGetConfigApi("ad_config"))
            .request(object : OnHttpListener<HttpData<CommonGetConfigApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<CommonGetConfigApi.Bean>?) {
                    adEnableStatus = result?.getData()?.enableStatus
                }

                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    private fun getVideoData(startUnlockEp: Int = 0) {
        loadMovieDataStatus = true
        if (startUnlockEp == 0) {
            setVideoLoadState(true)
        }
        val url = if (lang == null) MOVIE_DETAIL_URL else AD_MOVIE_DETAIL_URL
        EasyHttp.get(this).api(MovieDetailApi(movieId, lang).setUrl(url))
            .request(object : OnHttpListener<HttpData<MovieDetailApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<MovieDetailApi.Bean>?) {
                    loadMovieDataStatus = false
                    binding.backBtn.visibility = View.GONE
                    binding.errorLl.rootError.visibility = View.GONE
                    binding.playerVp.visibility = View.VISIBLE
                    result?.getData()?.apply {
                        mMovieDetailBean = this
                        setVideoData(this, startUnlockEp)
                    }

                    //剧广告开关打开则创建广告
                    if (result?.getData()?.s2s?.enableStatus == true) {
                        loadAd()
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    loadMovieDataStatus = false
                    binding.errorLl.rootError.visibility = View.VISIBLE
                    binding.errorLl.errorIv.setBackgroundResource(R.drawable.video_network_error_ic)
                    setVideoLoadState(false)
                    if (mMovieDetailBean == null) {
                        toast(throwable?.message)
                    }
                }
            })
    }

    fun setVideoLoadState(state: Boolean) {
        if (state) {
            binding.movieLoadingLav.playAnimation()
            binding.movieLoadingLav.visibility = View.VISIBLE
        } else {
            binding.movieLoadingLav.visibility = View.GONE
            binding.movieLoadingLav.cancelAnimation()
        }
    }

    /**
     * 设置剧集数据
     *
     * @param startUnlockEp 大于0代表解锁全集，然后裁剪开始解锁之前的剧集拼接新返回的剧集
     */
    private fun setVideoData(data: MovieDetailApi.Bean, startUnlockEp: Int = 0) {
        movieCode = data.code.toString()
        totalEpisode = data.totalEpisode
        data.list?.let { list ->
            if (list.isNotEmpty()) {
                startEpisode = list[0].ep
            }

            if (startUnlockEp > 0) {
                Logger.e("startUnlockEp:$startUnlockEp  list.size:${list.size}")
                if (startUnlockEp < list.size) {
                    val startUnlockIndex =
                        if (startEpisode == 0) startUnlockEp + 1 else startUnlockEp
                    val addList = list.subList(startUnlockIndex, list.size)
                    mAdapter.addData(addList)
                }
            } else {
                setEpData(data)
            }

            setAdapterLoadMore()

            initEpDialogData(data, list)

            MMKVExt.getDurableMMKV()?.apply {
                //用户第一次使用显示新手引导
                if (!decodeBool(KEY_VIDEO_GUIDE) && !isFinishing) {
                    setGuideVisibility(true)
                    postDelayed({
                        if (isFinishing.not()) setGuideVisibility(false)
                    }, 5000)
                    encode(KEY_VIDEO_GUIDE, true)
                }
            }
        }
    }

    private fun setGuideVisibility(visible: Boolean) {
        if (visible) {
            binding.guideLl.visibility = View.VISIBLE
            binding.guideLav.playAnimation()
        } else {
            binding.guideLl.visibility = View.GONE
            binding.guideLav.cancelAnimation()
        }
    }

    /**初始化剧集选择弹窗数据*/
    private fun initEpDialogData(data: MovieDetailApi.Bean, list: MutableList<VideoModel>) {
        val type = data.activity?.type ?: -1
        val activityStatus = data.activity?.activityStatus ?: -1
        val listSize = data.list?.size ?: 0

        data.activity?.apply {
            if (type == 0 && activityStatus == 0 && remindStatus == 0) {
                mFirstDramaDialog = FirstDramaDialog.Builder(this@PlayerActivity, data)
                mFirstDramaDialog?.addOnDismissListener(object : BaseDialog.OnDismissListener {
                    override fun onDismiss(dialog: BaseDialog?) {
                        backInterception(isAutoPlay)
                    }
                })
            }
        }

        // 生成剧集弹窗剧集集数分类数据
        val numberEpisodes = getContext().resources.getStringArray(
            if (startEpisode == 0) {
                if (listSize > 1) R.array.number_first_episodes else R.array.zero_first_episodes
            } else {
                R.array.number_episodes
            }
        )

        if (numberEpisodes.size > 1) {
            val classificationNum = totalEpisode / 30
            val numberExtractSurplus = totalEpisode % 30

            for (i in 0..<classificationNum) {
                mNumberEpisodesList.add(NumberEpisodesBean(i == 0, numberEpisodes[i]))
            }

            //还有小于30集的剧集拼接上去
            if (numberExtractSurplus > 0) {
                mNumberEpisodesList.add(
                    NumberEpisodesBean(
                        false, ((30 * classificationNum) + 1).toString() + "–" + totalEpisode
                    )
                )
            }
        } else {
            mNumberEpisodesList.add(NumberEpisodesBean(true, numberEpisodes[0]))
        }

        actualTotalEpisode =
                //如果活动开始，总集数加一级预播剧，没开始则只有一级预播剧，没有活动正常总集数
                //活动开始可能没有活动对象 直接用第一集是否是EP.0判断
            if (startEpisode == 0) if (listSize > 1) totalEpisode + 1 else 1 else totalEpisode

        //生成剧集播放弹窗列表
        for (i in 0..<actualTotalEpisode) {
            val isUnlock: Boolean = if (i < list.size) list[i].unlockStatus == UNLOCKED else false
            //默认Ep下标从1开始，需要加一。如果是第一集Ep为0则不用增加
            val currentEpisode = if (startEpisode == 0) i else i + 1
            val isVipFree = data.type == 0 && currentEpisode >= data.unlockStartEp && isUnlock
            isVip = isVipFree

            val number =
                if (startEpisode == 0) if (i == 0) getString(R.string.trailer) else i.toString()
                else (i + 1).toString()

            mEpisodesPlayList.add(
                EpisodesPlayBean(i == currentPage, isUnlock, number, isVipFree)
            )
        }
    }

    private fun setAdapterLoadMore() {
        mAdapter.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore(smartAdapter: SmartViewPager2Adapter<*>) {
                //滑动到preLoadLimit后触发底部加载监听
                Logger.e("到底了，要预加载数据")
                if (mAdapter.getLastItem()?.unlockStatus == UNLOCKED) {
                    loadNextEp()
                }
            }
        })
    }

    private fun timeInterval(): Boolean {
        val mInterceptionTime =
            MMKVExt.getDurableMMKV()?.decodeLong(KEY_INTERCEPT_EPISODES_SHOW_TIME) ?: 0
        //展示过后10分钟不出现拦截剧
        return System.currentTimeMillis() - mInterceptionTime > 1000 * 60 * 10
    }

    fun backInterception(isAutoPlay: Boolean = false) {
        this.isAutoPlay = isAutoPlay

        mMovieDetailBean?.activity?.apply {
            if (type == 0 && activityStatus == 0 && remindStatus == 0) {
                remindStatus = 1
                mFirstDramaDialog?.show()
                return
            }
        }

        //如果是自动播放或者是满足了间隔时间要求并且有数据和拦截剧UI没有显示就展示拦截剧
        if ((isAutoPlay || timeInterval()) && mInterceptionVideoList.isNotEmpty() && binding.interceptionDramaLl.visibility == View.GONE) {
            MMKVExt.getDurableMMKV()
                ?.encode(KEY_INTERCEPT_EPISODES_SHOW_TIME, System.currentTimeMillis())
            binding.interceptionDramaVp.adapter = mInterceptionAdapter
            binding.interceptionDramaLl.visibility = View.VISIBLE
            binding.backBtn.visibility = View.VISIBLE
            if (isAutoPlay) {
                autoPlayInterception = true
                // 5s内无操作，直接进入第一部拦截剧
                postDelayed(autoPlayRunnable, 5000)
            }
        } else {
            if (isAutoPlay.not()) finish()
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        binding.playerVp.unregisterOnPageChangeCallback(mOnPageChangeCallback)
        binding.interceptionDramaVp.unregisterOnPageChangeCallback(mOnPageChangeCallback2)
        binding.playerVp.adapter = null
        binding.interceptionDramaVp.adapter = null
        super.onDestroy()
        rewardedAd?.destroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            Logger.e("KEY_MOVIE_ID:" + getString(KEY_MOVIE_ID))
            getString(KEY_MOVIE_ID)?.let { movieId ->
                removeData(movieId)
            }
        }
    }

    private fun removeData(movieId: String) {
        binding.interceptionDramaVp.adapter = null
        binding.interceptionDramaLl.visibility = View.GONE

        for (i in mAdapter.getData().size - 1 downTo 0) {
            mAdapter.removeData(i)
        }

        this.movieId = movieId

        resetVideoData()
    }

    private fun resetVideoData(startUnlockEp: Int = 0) {
        mMovieDetailBean = null
        lastEpisode = null
        currentPage = 0
        mNumberEpisodesList.clear()
        mEpisodesPlayList.clear()
        if (startUnlockEp == 0) {
            loadInterceptionVideoList = false
            mInterceptionVideoList.clear()
        }
        getVideoData(startUnlockEp)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: UnlockNotify) {
        notify.apply {
            if (unlockType == 1) {
                mEpisodesPlayList[currentEp - startEpisode].isUnlock = true
                mAdapter.getItem(currentPage).unlockStatus = UNLOCKED
                sendWatchingNotify(mAdapter.getItem(currentPage).ep)
                loadNextEp()
            } else if (unlockType == 2) {
                resetVideoData(currentEp)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: SelectEpisodesNotify) {
        setCurrentEp(notify.number, notify.smoothScroll)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: InterceptionPlayEndNotify) {
        binding.interceptionDramaVp.currentItem += 1
    }

    private fun setCurrentEp(position: Int, smoothScroll: Boolean = false) {
        binding.playerVp.setCurrentItem(position, smoothScroll)
    }

    /**加载下一集*/
    private fun loadNextEp() {
        val url = if (lang == null) EP_DETAIL_URL else AD_EP_DETAIL_URL
        val episodes = mAdapter.getData().size + startEpisode

        EasyHttp.get(this@PlayerActivity)
            .api(EpisodesDetailApi(movieId, episodes, lang).setUrl(url))
            .request(object : OnHttpListener<HttpData<VideoModel>> {
                override fun onHttpSuccess(result: HttpData<VideoModel>?) {
                    result?.getData()?.let { videoModel ->
                        videoModel.wallet?.apply {
                            UserProfileHelper.setCoinsBonus(balance, integral)
                        }
                        //if (mAdapter.getLastItem()?.ep == videoModel.ep) return
                        mAdapter.addData(videoModel)
                        if (videoModel.unlockStatus == UNLOCKED) {
                            mEpisodesPlayList[videoModel.ep - startEpisode].isUnlock = true
                        }

                    }
                    loadNextStatus = true
                }

                override fun onHttpFail(throwable: Throwable?) {
                    mAdapter.finishLoadMore()
                    loadNextStatus = false
                }
            })
    }

    /**此方法需要滚动到对应集数*/
    private fun setEpData(data: MovieDetailApi.Bean) {
        data.list?.let {
            //如果外部传了当前剧集就有外部的 没有则使用历史观看剧集
            mCurrentEpisode = if (getInt(KEY_CURRENT_EPISODE) > 0) getInt(KEY_CURRENT_EPISODE)
            else data.watchHistory?.currentEpisode ?: 1
            /**下标从0开始 服务端是1开始 所以取值要减1 1.4.0新增第0集*/
            mCurrentEpisodePosition = mCurrentEpisode.minus(startEpisode)
            if (mCurrentEpisodePosition == -1) mCurrentEpisodePosition = 0

            if (mCurrentEpisodePosition < it.size) {
                it[mCurrentEpisodePosition].isFirst = true
            }

            mAdapter.addData(it)

            setCurrentEp(mCurrentEpisodePosition)
        }
    }

    /**观看上报*/
    private fun collect(episode: Int, lastEpisode: Int?) {
        EasyHttp.post(this@PlayerActivity).api(MovieCollectApi(movieCode, episode, lastEpisode, 1))
            .request(object : OnHttpListener<HttpData<VideoModel>> {
                override fun onHttpSuccess(result: HttpData<VideoModel>?) {
                    sendWatchingNotify(episode)

                    if (mInterceptionVideoList.isEmpty() && loadInterceptionVideoList.not()) {
                        getInterceptList()
                    }

                    if (mAdapter.itemCount > currentPage && mAdapter.getItem(currentPage).unlockStatus == PRE_UNLOCK) {
                        mAdapter.getItem(currentPage).unlockStatus = UNLOCKED
                        mEpisodesPlayList[currentPage].isUnlock = true
                        loadNextEp()
                    }
                    collectStatus = true
                }

                override fun onHttpFail(throwable: Throwable?) {
                    collectStatus = false
                }
            })
    }

    /**通知更新当前观看剧集*/
    private fun sendWatchingNotify(episode: Int) {
        val listBean = MovieListApi.Bean.ListBean()
        listBean.apply {
            movieId = this@PlayerActivity.movieId
            title = mMovieDetailBean?.title
            poster = mMovieDetailBean?.poster
            totalEpisode = mMovieDetailBean?.totalEpisode
            watchEpisode = episode
        }
        EventBus.getDefault().post(WatchingNotify(listBean))
    }

    /**刷新余额*/
    fun getWalletBalance() {
        EasyHttp.get(this).api(WalletBalanceApi())
            .request(object : OnHttpListener<HttpData<WalletBalanceApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<WalletBalanceApi.Bean>?) {
                    result?.getData()?.apply {
                        UserProfileHelper.setCoinsBonus(balance, integral)
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    /**
     * 广告观看上报
     * @param watchingStatus 是否观看成功
     */
    private fun adTrigger(
        watchingStatus: Boolean,
        revenue: Double? = null,
        networkName: String? = null,
        adLabel: String? = null
    ) {
        EasyHttp.post(this@PlayerActivity).api(
            AdTriggerApi(
                MAX_AD_UNIT_ID,
                if (watchingStatus) watchAdCount else -1,
                movieId,
                mCurrentEpisode,
                traceId,
                1,
                revenue,
                networkName,
                adLabel
            )
        ).request(object : OnHttpListener<HttpData<AdTriggerApi.Bean>> {
            override fun onHttpSuccess(result: HttpData<AdTriggerApi.Bean>?) {
                result?.getData()?.let {
                    traceId = it.traceId.toString()

                    if (watchingStatus) {
                        val singleUnlock = mMovieDetailBean?.s2s?.singleUnlock ?: 1
                        EventBus.getDefault().post(
                            WatchingAdSuccessNotify(
                                watchAdCount,
                                it.todayUnlockCount,
                                mCurrentEpisode,
                                traceId,
                                singleUnlock
                            )
                        )
                    }

                    if (watchAdCount == mMovieDetailBean?.s2s?.viewCount) {
                        watchAdCount = 0
                    } else {
                        watchAdCount++
                    }
                }
            }

            override fun onHttpFail(throwable: Throwable?) {}
        })
    }

    private fun createRewardedAd() {
        //传入当前的activity 广告SDK不会释放导致内存泄露
        ActivityManager.getInstance().getActivity(HomeActivity::class.java)?.let {
            rewardedAd = MaxRewardedAd.getInstance(MAX_AD_UNIT_ID, it)
        }
    }

    private fun loadAd() {
        try {
            rewardedAd?.setListener(this)
            rewardedAd?.loadAd()
        } catch (e: Exception) {
            Logger.e("MAX广告初始化失败:${e.message}")
        }
    }

    fun showAd() {
        if (rewardedAd?.isReady == true) {
            adIsReady = true
            ActivityManager.getInstance().getActivity(HomeActivity::class.java)?.let {
                rewardedAd?.showAd(it)
            }
        } else {
            adIsReady = false
            showDialog()
        }
    }

    // MAX Ad Listener
    override fun onUserRewarded(maxAd: MaxAd, maxReward: MaxReward) {
        // 已显示奖励广告，用户应获得奖励
        Logger.d(
            "onAdRewarded.adUnitId:${maxAd.adUnitId} ,revenue:${maxAd.revenue}" + ",format:${maxAd.format.label} ,networkName:${maxAd.networkName}"
        )
        adTrigger(true, maxAd.revenue, maxAd.networkName, maxAd.format.label)
    }

    override fun onAdLoaded(maxAd: MaxAd) {
        // 已准备好播放奖励广告。rewardedAd.isReady（）现在将返回“true”
        // Reset retry attempt
        retryAttempt = 0.0

        val waterfall = maxAd.waterfall
        Logger.d("onAdLoaded:Waterfall Name: " + waterfall.name + " and Test Name: " + waterfall.testName)
        Logger.d("onAdLoaded:Waterfall latency was: " + waterfall.latencyMillis + " milliseconds")

        var waterfallInfoStr = ""
        for (networkResponse in waterfall.networkResponses) {
            waterfallInfoStr =
                "Network -> ${networkResponse.mediatedNetwork}" + "\n...adLoadState: ${networkResponse.adLoadState}" + "\n...latency: ${networkResponse.latencyMillis} milliseconds" + "\n...credentials: ${networkResponse.credentials}"
            if (networkResponse.error != null) {
                waterfallInfoStr += "\n...error: ${networkResponse.error}"
            }
        }

        adIsReady?.let { adIsReady ->
            if (adIsReady.not()) {
                hideDialog()
                showAd()
            }
        }

        Logger.d("onAdLoaded:$waterfallInfoStr")
    }

    override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
        toast(R.string.new_ad)
        // Rewarded ad failed to load
        // 我们建议以指数级更高的延迟重试，最大延迟为64秒
        retryAttempt++
        val delayMillis =
            TimeUnit.SECONDS.toMillis(2.0.pow(6.0.coerceAtMost(retryAttempt)).toLong())

        postDelayed({ rewardedAd?.loadAd() }, delayMillis)

        val waterfall = error.waterfall ?: return
        Logger.e("onAdLoadFailed:Waterfall Name: " + waterfall.name + " and Test Name: " + waterfall.testName)
        Logger.e("onAdLoadFailed:Waterfall latency was: " + waterfall.latencyMillis + " milliseconds")

        for (networkResponse in waterfall.networkResponses) {
            Logger.e(
                "onAdLoadFailed.Network -> ${networkResponse.mediatedNetwork}" + "...latency: ${networkResponse.latencyMillis} milliseconds" + "...credentials: ${networkResponse.credentials}" + "...error: ${networkResponse.error}"
            )
        }
    }

    override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
        // 无法显示奖励广告。我们建议加载下一个广告
        rewardedAd?.loadAd()
        adTrigger(false)
        Logger.e("onAdDisplayFailed. error:${error.message}")
    }

    override fun onAdDisplayed(maxAd: MaxAd) {
        Logger.e("onAdDisplayed")
    }

    override fun onAdClicked(maxAd: MaxAd) {
        Logger.e("onAdClicked")
    }

    override fun onAdHidden(maxAd: MaxAd) {
        // rewarded ad is hidden. Pre-load the next ad
        rewardedAd?.loadAd()
        Logger.e("onAdHidden.adUnitId:${maxAd.adUnitId}")
    }

}