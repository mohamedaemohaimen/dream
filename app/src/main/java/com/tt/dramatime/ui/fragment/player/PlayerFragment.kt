package com.tt.dramatime.ui.fragment.player

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.LogUtils
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.google.android.gms.ads.AdView
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.smart.adapter.interf.SmartFragmentImpl
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXVodConstants
import com.tt.base.BaseDialog
import com.tt.dramatime.R
import com.tt.dramatime.app.AppApplication
import com.tt.dramatime.app.BasePlayerFragment
import com.tt.dramatime.databinding.PlayFragmentBinding
import com.tt.dramatime.http.api.ClientReportLogApi
import com.tt.dramatime.http.api.DiscountPopupApi
import com.tt.dramatime.http.api.EpisodesDetailApi
import com.tt.dramatime.http.api.HttpUrls.Companion.AD_EP_DETAIL_URL
import com.tt.dramatime.http.api.HttpUrls.Companion.EP_DETAIL_URL
import com.tt.dramatime.http.api.TaskProgressApi
import com.tt.dramatime.http.api.UnlockMovieApi
import com.tt.dramatime.http.api.UserInfoApi
import com.tt.dramatime.http.bean.VideoModel
import com.tt.dramatime.http.db.CommonAdConfigHelper
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_CONTENT_LANGUAGE
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.MovieDetailHelper
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.google.ad.BannerAdManager
import com.tt.dramatime.manager.player.TXCSDKService
import com.tt.dramatime.manager.player.TXVodPlayerWrapper
import com.tt.dramatime.other.AppConfig
import com.tt.dramatime.ui.activity.player.BonusActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity.Companion.INSUFFICIENT_COINS
import com.tt.dramatime.ui.activity.player.PlayerActivity.Companion.NOT_UNLOCKED
import com.tt.dramatime.ui.activity.player.PlayerActivity.Companion.UNLOCKED
import com.tt.dramatime.ui.dialog.limit.EpDiscountsDialog
import com.tt.dramatime.ui.dialog.limit.LimitDiscountsDialog
import com.tt.dramatime.ui.dialog.limit.LimitOneDiscountsDialog
import com.tt.dramatime.ui.dialog.limit.VipDiscountsDialog
import com.tt.dramatime.ui.dialog.player.EnableNotificationsDialog
import com.tt.dramatime.ui.dialog.player.EpisodesPlayDialog
import com.tt.dramatime.ui.dialog.player.PlayerMoreDialog
import com.tt.dramatime.ui.dialog.player.StoreDialog
import com.tt.dramatime.ui.dialog.player.UnlockDialog
import com.tt.dramatime.ui.dialog.player.VipExpiredDialog
import com.tt.dramatime.util.DoubleClickListener
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.util.eventbus.MovieDetailNotify
import com.tt.dramatime.util.eventbus.PaySuccessNotify
import com.tt.dramatime.util.eventbus.SelectEpisodesNotify
import com.tt.dramatime.util.eventbus.StopPlayNotify
import com.tt.dramatime.util.eventbus.UnlockNotify
import com.tt.dramatime.util.eventbus.WatchingAdSuccessNotify
import com.tt.dramatime.util.subtitleFile.srt.SRTParser
import com.tt.dramatime.util.subtitleFile.srt.Subtitle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale
import kotlin.math.abs


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 播放器Fragment
 * </pre>
 */
class PlayerFragment : BasePlayerFragment(), SmartFragmentImpl<VideoModel>, OnSeekBarChangeListener,
    TXVodPlayerWrapper.OnPlayEventChangedListener {

    companion object {
        const val TAG = "PlayerFragment"
        const val INTENT_KEY_PARAMETERS: String = "parameters"
    }

    private lateinit var mPlayerActivity: PlayerActivity
    private lateinit var binding: PlayFragmentBinding
    private lateinit var mVideoModel: VideoModel

    private var isVisible = false
    private var isStop = false
    private var mManualPause = false
    private var isLoadEpInfo = false
    private var mStartSeek = false
    private var mStoreDialogShow = false
    private var mVipExpiredDialogShow = false
    private var mUnlockDialogShow = false
    private var setDoubleClickListener = false
    private var mTrackingTouchTS: Long = 0
    private var getPlayInfoFailCount: Int = 0
    private var reportStatus: Int = 0
    private var reportFailCount: Int = 0

    private var mDiscountPopupShowTime: Long = 0

    private var bannerAdView: AdView? = null

    private var freeAdUnlock = false

    private var isWatchAd = false

    private val mTXVodPlayerWrapper by lazy(LazyThreadSafetyMode.NONE) {
        TXVodPlayerWrapper(context, mVideoModel)
    }

    private val mStoreDialog by lazy(LazyThreadSafetyMode.NONE) {
        context?.let {
            mPlayerActivity.mMovieDetailBean?.type?.let { type ->
                StoreDialog.Builder(
                    it,
                    mVideoModel.movieCode,
                    mVideoModel.ep,
                    mPlayerActivity.mEpisodesPlayList.size,
                    type
                )
            }
        }
    }

    private val mVipExpiredDialog by lazy(LazyThreadSafetyMode.NONE) {
        context?.let {
            VipExpiredDialog.Builder(it, mVideoModel.movieCode, mVideoModel.ep)
        }
    }

    private val mUnlockDialog by lazy(LazyThreadSafetyMode.NONE) {
        context?.let {
            mPlayerActivity.mMovieDetailBean?.s2s?.viewCount?.let { viewCount ->
                mPlayerActivity.mMovieDetailBean?.s2s?.singleUnlock.let { singleUnlock ->
                    UnlockDialog.Builder(it, viewCount, singleUnlock)
                }
            }
        }
    }

    private val mDoubleClickListener by lazy(LazyThreadSafetyMode.NONE) {
        setDoubleClickListener = true
        DoubleClickListener()
    }

    private val controlRunnable = Runnable {
        setPlayControlVisible(false)
        pauseIvGone()
    }

    private val playerRunnable = Runnable {
        binding.pauseIv.visibility = View.GONE
    }

    /**
     * 根据资源 id 获取一个 View 对象
     */
    override fun <V : View?> findViewById(@IdRes id: Int): V? {
        return binding.root.findViewById(id)
    }

    override fun initSmartFragmentData(bean: VideoModel) {
        this.mVideoModel = bean
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = PlayFragmentBinding.inflate(inflater, container, false)
        if (EventBus.getDefault().isRegistered(this).not()) {
            EventBus.getDefault().register(this)
        }
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INTENT_KEY_PARAMETERS, mVideoModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            // 恢复数据 如果需要恢复进度 要额外保存进度数据
            val videoModel: VideoModel? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    savedInstanceState.getParcelable(INTENT_KEY_PARAMETERS, VideoModel::class.java)
                } else {
                    savedInstanceState.getParcelable(INTENT_KEY_PARAMETERS)
                }
            videoModel?.let {
                mVideoModel = videoModel
            }
        }
    }

    private fun cantPlay(): Boolean {
        return mVideoModel.unlockStatus == NOT_UNLOCKED || mVideoModel.unlockStatus == INSUFFICIENT_COINS
    }

    /**复用时只会走一次，被回收时回到该界面会重新走*/
    override fun initView() {
        mPlayerActivity = (activity as PlayerActivity)

        LogUtils.dTag(
            TAG,
            "视频初始化及预加载.ep:${mVideoModel.ep} CurrentEpisode:${mPlayerActivity.mCurrentEpisode}"
        )

        context?.let { GlideUtils.loadImageBlack(it, mVideoModel.cover, binding.coverIv) }

        setEpInfo()

        setPlayerView()

        preStartPlay()

        if (mVideoModel.isFirst && cantPlay().not()) {
            setPlayControlVisible(true)
            binding.pauseIv.visibility = View.VISIBLE
        }

        mDoubleClickListener.setSingleClickListener {
            singleTap()
        }
        mDoubleClickListener.setDoubleClickListener {
            doubleTap()
        }

        if (cantPlay()) {
            binding.unlockCl.visibility = View.VISIBLE
            binding.playerTopGp.visibility = View.VISIBLE
            UserProfileHelper.apply {
                binding.unlockBalanceTv.text = getString(
                    R.string.balance_coins_bonus, getCoins(), getBonus()
                )
            }

            mStoreDialog?.addOnDismissListener(object : BaseDialog.OnDismissListener {
                override fun onDismiss(dialog: BaseDialog?) {
                    if (binding.bonusFl.visibility == View.GONE && mPlayerActivity.adEnableStatus == true) binding.bonusFl.visibility =
                        View.VISIBLE

                    if (mStoreDialog?.isPay == false && System.currentTimeMillis() - mDiscountPopupShowTime > 15 * 1000 && isAdded) {
                        getDiscountPopup()
                    }
                }
            })

            //屏蔽IAA模块代码
            mPlayerActivity.mMovieDetailBean?.s2s?.apply {

                freeAdUnlock = !limitStatus || todayUnlock < maxUnlock

                if (enableStatus && mVideoModel.ep <= unlockEp && freeAdUnlock) {
                    binding.watchUnlockCl.visibility = View.VISIBLE

                    val s2s = mPlayerActivity.mMovieDetailBean?.s2s

                    binding.watchUnlockTv.text = s2s?.let {
                        if (it.singleUnlock > 1) getString(
                            R.string.watch_ads_to_unlock_ep_num, it.singleUnlock, 0, viewCount
                        ) else getString(
                            R.string.watch_ads_to_unlock_num, 0, viewCount
                        )
                    } ?: getString(R.string.watch_ads_to_unlock_num, 0, viewCount)

                    binding.watchTodayTv.visibility = View.VISIBLE
                    binding.watchTodayTv.text =
                        if (limitStatus) getString(R.string.change_num, maxUnlock - todayUnlock)
                        else getString(R.string.unlimited)

                    //如果是无限制解锁 调换按钮顺序
                    if (!limitStatus) {
                        binding.watchUnlockCl.setBackgroundResource(R.drawable.button_big_45_bg)
                        binding.unlockFl.setBackgroundResource(R.drawable.button_big_transparent_45_bg)

                        val constraintSet = ConstraintSet()
                        constraintSet.clone(binding.unlockCl)
                        constraintSet.connect(
                            R.id.watch_unlock_cl,
                            ConstraintSet.TOP,
                            R.id.unlock_hint_tv,
                            ConstraintSet.BOTTOM,
                            dp2px(18f)
                        )

                        constraintSet.connect(
                            R.id.unlock_fl,
                            ConstraintSet.TOP,
                            R.id.watch_unlock_cl,
                            ConstraintSet.BOTTOM,
                            dp2px(18f)
                        )

                        constraintSet.connect(
                            R.id.unlock_balance_tv,
                            ConstraintSet.TOP,
                            R.id.unlock_fl,
                            ConstraintSet.BOTTOM,
                            dp2px(15f)
                        )
                        constraintSet.applyTo(binding.unlockCl)

                        mUnlockDialog?.setOnWatchAdCallback(object :
                            UnlockDialog.OnWatchAdCallback {
                            override fun watchAd() {
                                mPlayerActivity.showAd()
                            }

                            override fun adFree() {
                                mStoreDialogShow = true
                                mStoreDialog?.show()
                            }
                        })
                    }
                }
            }
        } else {
            binding.touchView.setOnClickListener(mDoubleClickListener)
            //屏蔽IAA模块代码
            loadBannerAd()
        }

        binding.seekbarShortVideo.setOnSeekBarChangeListener(this)

        if (binding.playerControlGp.visibility == View.VISIBLE) {
            postDelayed(controlRunnable, 5000)
        }

        setOnClickListener(
            binding.backBtn,
            binding.moreBtn,
            binding.pauseIv,
            binding.episodesInfoLl,
            binding.unlockFl,
            binding.watchUnlockCl,
            binding.bonusFl
        )
    }

    private fun loadBannerAd() {
        if (CommonAdConfigHelper.getBannerAdEnableStatus()) {
            bannerAdView = AdView(AppApplication.appContext).apply {
                val bannerViewLayoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
                layoutParams = bannerViewLayoutParams
                // 把 Banner Ad 添加到根布局
                binding.adViewFl.addView(this)
                BannerAdManager().createBannerAdView(this)
            }
        }
    }

    override fun lazyInit() {
        super.lazyInit()
        //懒加载位置
        LogUtils.dTag(TAG, "页面懒加载.mVideoModel.ep==${mVideoModel.ep}")
    }

    override fun onVisible() {
        LogUtils.dTag(TAG, "页面可见时播放视频位置.mVideoModel.ep==${mVideoModel.ep}")
        isVisible = true

        if (cantPlay()) {
            mPlayerActivity.mMovieDetailBean?.s2s?.apply {

                val mStoreDialogStatus = mStoreDialog?.isShowing() == false && !mStoreDialogShow
                // 未开启广告解锁的时候现实商城弹窗
//                if (mStoreDialogStatus) {
                //屏蔽IAA模块代码
                if (mStoreDialogStatus && !enableStatus && !isWatchAd || (limitStatus && !isWatchAd)) {
                    mStoreDialogShow = true
                    mStoreDialog?.show()
                }

                //屏蔽IAA模块代码
                val mUnlockDialogStatus =
                    mUnlockDialog?.isShowing()?.not() == true && mUnlockDialogShow.not()

                if (enableStatus && mVideoModel.ep <= unlockEp && !limitStatus && mUnlockDialogStatus) {
                    mUnlockDialogShow = true
                    mUnlockDialog?.show()
                }

            }

            val paymentAmount = mPlayerActivity.mMovieDetailBean?.paymentAmount ?: 0
            UserProfileHelper.apply {
                if (cantPlay() && getCoins() + getBonus() >= paymentAmount) unlock(false)
            }
            return
        }

        if (mPlayerActivity.isVip && UserProfileHelper.getVipState().not()) {
            getInfo()
        }

        if (CommonAdConfigHelper.getBannerAdEnableStatus()
                .not() && binding.adViewFl.visibility == View.VISIBLE
        ) {
            binding.adViewFl.visibility = View.GONE
        }

        if (mManualPause) return

        if (binding.playerControlGp.visibility == View.GONE) {
            binding.pauseIv.visibility = View.GONE
        }

        startPlay()
    }

    override fun onInVisible() {
        //如果想每次回到视频都从第一帧开始，也可以在这里处理
        LogUtils.dTag(TAG, "页面不可见时暂停视频.mVideoModel.ep==" + mVideoModel.ep)
        isVisible = false
        if (mPlayerActivity.mCurrentEpisode == mVideoModel.ep) {
            pausePlayer()
        } else {
            mStoreDialogShow = false
            mVipExpiredDialogShow = false
            mUnlockDialogShow = false
            //为了减少性能消耗可以改为stopPlayer()
            pausePlayer()
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
        stopPlayer()
        binding.tcvVideoView.onDestroy()
        binding.adViewFl.removeView(bannerAdView)
        mUnlockDialog?.setOnWatchAdCallback(null)
        bannerAdView?.destroy()
        isLoadEpInfo = false
        mManualPause = false
        isStop = false
        isVisible = false
        mStoreDialogShow = false
        mVipExpiredDialogShow = false
        LogUtils.dTag("PlayerFragment", "页面销毁.mVideoModel.ep==" + mVideoModel.ep)
    }

    @SingleClick(1000)
    override fun onClick(view: View) {
        context?.let { context ->
            when (view) {
                binding.backBtn -> {
                    stopPlayer()
                    mPlayerActivity.backInterception()
                }

                binding.moreBtn -> {
                    mPlayerActivity.mMovieDetailBean?.shareUrl?.let {
                        PlayerMoreDialog.Builder(context, mVideoModel.movieId, it).show()
                    }
                }

                binding.pauseIv -> playerOperation()

                binding.episodesInfoLl -> {
                    mPlayerActivity.mMovieDetailBean?.apply {
                        EpisodesPlayDialog.Builder(
                            context,
                            mPlayerActivity.mNumberEpisodesList,
                            mPlayerActivity.mEpisodesPlayList,
                            mPlayerActivity.mMovieDetailBean?.totalEpisode,
                            title,
                            viewNum,
                            mVideoModel.ep
                        ).show()
                    }
                }

                binding.unlockFl -> {
                    val paymentAmount = mPlayerActivity.mMovieDetailBean?.paymentAmount ?: 0
                    UserProfileHelper.apply {
                        if (getCoins() + getBonus() >= paymentAmount) unlock(false) else {
                            mStoreDialogShow = true
                            mStoreDialog?.show()
                        }
                    }
                }

                binding.watchUnlockCl -> {
                    mPlayerActivity.showAd()
                    isWatchAd = true
                }

                binding.bonusFl -> {
                    BonusActivity.start(context)
                }

                else -> {}
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: StopPlayNotify) {
        stopPlayer()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: MovieDetailNotify) {
        //没有缓存的清况下接口访问成功设置剧集信息
        setEpInfo()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: PaySuccessNotify) {
        notify.apply {
            if (currentEpisode == mVideoModel.ep) {
                if (mStoreDialog?.isShowing() == true) {
                    mStoreDialog?.isPay = true
                    mStoreDialog?.dismiss()
                }

                //支付成功刷新金币积分数量 并且解锁
                UserProfileHelper.apply {
                    binding.unlockBalanceTv.text = getString(
                        R.string.balance_coins_bonus, getCoins(), getBonus()
                    )
                }

                if (showNoticeDialog) {
                    EnableNotificationsDialog.Builder(requireContext(), this@PlayerFragment)
                        .addOnDismissListener(object : BaseDialog.OnDismissListener {
                            override fun onDismiss(dialog: BaseDialog?) {
                                unlockDispose()
                            }
                        }).show()
                } else {
                    unlockDispose()
                }
            }
        }
    }

    private fun PaySuccessNotify.unlockDispose() {
        when (unlockType) {
            1 -> unlock(false)
            2 -> unlockSuccess(unlockType)
            3 -> startPlay()
            else -> mVideoModel.unlockStatus = UNLOCKED
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: WatchingAdSuccessNotify) {
        if (notify.currentEpisode == mVideoModel.ep) {
            mPlayerActivity.mMovieDetailBean?.s2s?.apply {
                binding.watchUnlockTv.text = if (singleUnlock > 1) getString(
                    R.string.watch_ads_to_unlock_ep_num, singleUnlock, notify.watchNum, viewCount
                ) else getString(
                    R.string.watch_ads_to_unlock_num, notify.watchNum, viewCount
                )

                if (limitStatus) {
                    todayUnlock = notify.todayUnlockCount
                    binding.watchTodayTv.text =
                        getString(R.string.change_num, maxUnlock - todayUnlock)
                }

                if (cantPlay()) {
                    if (notify.watchNum == viewCount) {
                        mUnlockDialog?.dismiss()
                        unlock(true, notify.traceId, notify.singleUnlock)
                    } else if (mUnlockDialog?.isShowing() == true) {
                        mUnlockDialog?.setWatchNum(notify.watchNum)
                    }
                }
            }
        }
    }

    /**解锁上报*/
    private fun unlock(isAd: Boolean, traceId: String? = null, singleUnlock: Int = 1) {
        showDialog()
        EasyHttp.post(this).api(
            UnlockMovieApi(mVideoModel.movieCode, mVideoModel.ep, if (isAd) "s2s" else "", traceId)
        ).request(object : OnHttpListener<HttpData<Void>> {
            override fun onHttpSuccess(result: HttpData<Void>?) {
                hideDialog()
                val unlockType = if (singleUnlock > 1) 2 else 1
                if (isAd.not()) mPlayerActivity.getWalletBalance() else {
                    val unlockEp = if (singleUnlock > 1) getString(
                        R.string.ep_for_free, mVideoModel.ep, mVideoModel.ep + singleUnlock - 1
                    ) else mVideoModel.ep.toString()
                    toast(getString(R.string.unlocked_for_free, unlockEp))
                }
                unlockSuccess(unlockType)
            }

            override fun onHttpFail(throwable: Throwable?) {
                hideDialog()
                toast(throwable?.message)
            }
        })
    }

    /**任务进度上报*/
    private fun taskProgress() {
        if (reportStatus == 1 || reportFailCount > 3) return
        EasyHttp.post(this).api(TaskProgressApi(9, mVideoModel.movieCode, mVideoModel.ep))
            .request(object : OnHttpListener<HttpData<TaskProgressApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<TaskProgressApi.Bean>?) {
                    reportStatus = 2
                }

                override fun onHttpFail(throwable: Throwable?) {
                    reportFailCount++
                    reportStatus = 0
                }
            })
        reportStatus = 1
    }

    private fun getInfo() {
        EasyHttp.get(this).api(UserInfoApi())
            .request(object : OnHttpListener<HttpData<UserInfoApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UserInfoApi.Bean?>?) {
                    data?.getData()?.apply {
                        UserProfileHelper.setProfile(this)
                        if (UserProfileHelper.getVipState().not() && mVipExpiredDialog?.isShowing()
                                ?.not() == true && mVipExpiredDialogShow.not()
                        ) {
                            pausePlayer()
                            mManualPause = true
                            mVipExpiredDialogShow = true
                            mVipExpiredDialog?.show()
                        }
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    private fun getDiscountPopup() {
        showDialog(getString(R.string.gift_pack_is_coming))
        EasyHttp.get(this).api(DiscountPopupApi(1))
            .request(object : OnHttpListener<HttpData<DiscountPopupApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<DiscountPopupApi.Bean>?) {
                    hideDialog()
                    mDiscountPopupShowTime = System.currentTimeMillis()
                    result?.getData()?.apply {
                        val bean = this
                        goods?.apply {
                            context?.let { context ->
                                recharge?.let {
                                    if (it.size == 1) {
                                        LimitOneDiscountsDialog.Builder(context, bean, it, 6).show()
                                    } else if (it.size > 1) {
                                        LimitDiscountsDialog.Builder(context, bean, it, 6).show()
                                    }
                                }

                                unlock?.let {
                                    EpDiscountsDialog.Builder(context, bean, it, 6).show()
                                }

                                subscribe?.let {
                                    VipDiscountsDialog.Builder(context, id, it, 6).show()
                                }
                            }
                        }
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    hideDialog()
                }
            })
    }

    private fun unlockSuccess(unlockType: Int) {
        mVideoModel.unlockStatus = UNLOCKED
        MovieDetailHelper(mVideoModel.movieId).updateUnlock(mVideoModel.ep)
        EventBus.getDefault().post(UnlockNotify(mVideoModel.ep, unlockType))

        if (bannerAdView == null) {
            loadBannerAd()
        }

        binding.unlockCl.visibility = View.GONE
        binding.playerTopGp.visibility = View.GONE
        binding.touchView.setOnClickListener(mDoubleClickListener)
        startPlay()
    }

    /**设置剧集信息*/
    private fun setEpInfo() {
        if (!isLoadEpInfo) {
            mPlayerActivity.mMovieDetailBean?.let {
                binding.epCurrentTotalTv.text = getString(
                    R.string.ep_current_total, mVideoModel.ep, it.totalEpisode
                )
                binding.epNameTv.text = it.title
                isLoadEpInfo = true
            }
        }
    }

    private fun setPlayControlVisible(visible: Boolean) {
        if (visible) {
            binding.playerControlGp.visibility = View.VISIBLE
            binding.playerTopGp.visibility = View.VISIBLE
        } else {
            binding.playerControlGp.visibility = View.GONE
            binding.playerTopGp.visibility = View.GONE
        }
    }

    private fun setPlayerView() {
        mTXVodPlayerWrapper.setPlayerView(binding.tcvVideoView)
        mTXVodPlayerWrapper.setSubtitle(binding.txSubtitleView)
        mTXVodPlayerWrapper.setSubtitleStyle(mPlayerActivity.mMovieDetailBean?.subtitleStyle)

        mVideoModel.apply {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    if (subtitleBase64 == null) {
                        binding.txSubtitleView.visibility = View.VISIBLE
                        return@launch
                    } else binding.txSubtitleView.visibility = View.GONE

                    //生成一个内部文件路径
                    val path = mTXVodPlayerWrapper.createInternalFilePath(
                        requireContext(), "${movieCode}-${ep}.srt"
                    )

                    var subtitles: Deferred<ArrayList<Subtitle>?>? = null

                    //将base64解码写入文件
                    mTXVodPlayerWrapper.base64ToFile(subtitleBase64!!, path, onComplete = {
                        subtitles = async {
                            SRTParser.parseSRT(it)
                        }
                    }, onError = {
                        recordException(it, "onWriteError.Exception")
                    })

                    withContext(Dispatchers.Main) {
                        val mSubtitles = subtitles?.await()
                        if (mSubtitles?.isNotEmpty() == true) {
                            binding.subtitleView.setSubtitleData(
                                mSubtitles, mPlayerActivity.mMovieDetailBean?.subtitleStyle
                            )
                            binding.subtitleView.visibility = View.VISIBLE
                            binding.txSubtitleView.visibility = View.GONE
                        } else {
                            binding.txSubtitleView.visibility = View.VISIBLE
                            binding.subtitleView.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    binding.txSubtitleView.visibility = View.VISIBLE
                    binding.subtitleView.visibility = View.GONE
                    recordException(e, "字幕解析.Exception")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun VideoModel.recordException(e: Exception, exceptionTitle: String) {
        Firebase.crashlytics.recordException(
            Exception(
                "$exceptionTitle:${e.message}," + "用户ID：${UserProfileHelper.getUserId()},movieCode:$movieCode,ep:$ep,lang:${
                    MMKVExt.getDurableMMKV()?.getString(KEY_CONTENT_LANGUAGE, "")
                }"
            )
        )
    }

    private fun preStartPlay() {
        mTXVodPlayerWrapper.preStartPlay()
        mTXVodPlayerWrapper.setVodChangeListener(this)
        isStop = false
        Logger.i("[preStartPlay] mVideoModel.ep==" + mVideoModel.ep + "  mVideoModel.url " + mVideoModel.url)
    }

    private fun startPlay() {
        //如果停止了播放器要重新预播放
        if (isStop) {
            setPlayerView()
            mTXVodPlayerWrapper.setVodChangeListener(this)
        }
        mManualPause = false
        binding.pauseIv.setImageResource(R.drawable.player_suspend)
        mTXVodPlayerWrapper.resumePlay()
        if (setDoubleClickListener.not()) binding.touchView.setOnClickListener(mDoubleClickListener)
        Logger.i("[startPlay] mTXVodPlayerWrapper.url " + mTXVodPlayerWrapper.url)
    }

    private fun pausePlayer() {
        mTXVodPlayerWrapper.pausePlay()
    }

    private fun stopPlayer() {
        mTXVodPlayerWrapper.stopPlay()
        mTXVodPlayerWrapper.setVodChangeListener(null)
        Logger.i("[stopPlayer] mTXVodPlayerWrapper.url " + mTXVodPlayerWrapper.url)
        binding.pauseIv.visibility = View.GONE
        isStop = true
    }

    private fun stopForPlaying() {
        mTXVodPlayerWrapper.stopForPlaying()
        mTXVodPlayerWrapper.setVodChangeListener(null)
        Logger.i("[stopForPlaying] mTXVodPlayerWrapper.url " + mTXVodPlayerWrapper.url)
        binding.pauseIv.visibility = View.GONE
        binding.coverIv.visibility = View.VISIBLE
        isStop = true
    }

    private fun handlePlayProgress(param: Bundle) {
        if (mStartSeek) {
            return
        }
        val progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS)
        val duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION) //单位为s
        val progressMS = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS)
        val durationMS = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS)
        val playableDurationMS = param.getInt(TXLiveConstants.EVT_PLAYABLE_DURATION_MS)
        val curTS = System.currentTimeMillis()

        binding.subtitleView.seekTo(progressMS.toLong())

        if (abs(curTS - mTrackingTouchTS) < 500) {
            return
        }
        mTrackingTouchTS = curTS

        binding.seekbarShortVideo.max = durationMS
        binding.seekbarShortVideo.progress = progressMS
        binding.seekbarShortVideo.secondaryProgress = playableDurationMS

        if ((progress.toFloat() / duration.toFloat()) > 0.8 && reportStatus == 0) {
            taskProgress()
        }

        val tempString = String.format(
            Locale.getDefault(),
            "%02d:%02d / %02d:%02d",
            progress / 60,
            progress % 60,
            duration / 60,
            duration % 60
        )

        binding.tvProgressTime.text = tempString
    }

    private fun playerOperation() {
        if (mTXVodPlayerWrapper.isPlaying) {
            mManualPause = true
            binding.pauseIv.setImageResource(R.drawable.player_pause)
            removeCallbacks(playerRunnable)
            pausePlayer()
        } else {
            binding.pauseIv.setImageResource(R.drawable.player_suspend)
            postDelayed(playerRunnable, 3000)
            startPlay()
        }
    }

    private fun pauseIvGone() {
        if (mManualPause.not() && binding.pauseIv.visibility == View.VISIBLE) {
            binding.pauseIv.visibility = View.GONE
        }
    }

    private fun singleTap() {
        if (cantPlay()) return
        if (binding.playerControlGp.visibility == View.GONE) {
            setPlayControlVisible(true)
            binding.pauseIv.visibility = View.VISIBLE
            postDelayed(controlRunnable, 5000)
        } else {
            removeCallbacks(controlRunnable)
            setPlayControlVisible(false)
            pauseIvGone()
        }
    }

    private fun doubleTap() {
        if (cantPlay()) return
        if (binding.pauseIv.visibility == View.GONE) {
            binding.pauseIv.visibility = View.VISIBLE
        }
        playerOperation()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val tempString = String.format(
            Locale.CHINA,
            "%02d:%02d / %02d:%02d",
            progress / 1000 / 60,
            progress / 1000 % 60,
            seekBar.max / 1000 / 60,
            seekBar.max / 1000 % 60
        )
        binding.tvProgressTime.text = tempString
    }

    /**仿抖音触摸seekbar变粗*/
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            seekBar?.maxHeight = ConvertUtils.dp2px(8f)
            seekBar?.minHeight = ConvertUtils.dp2px(8f)
        }
        binding.tvProgressTime.visibility = View.VISIBLE
        mStartSeek = true
        onLoading(false)
    }

    /**停止触摸恢复原样*/
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mTXVodPlayerWrapper.seekTo(seekBar.progress / 1000f)
        binding.pauseIv.visibility = View.GONE
        mTrackingTouchTS = System.currentTimeMillis()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            seekBar.maxHeight = ConvertUtils.dp2px(2f)
            seekBar.minHeight = ConvertUtils.dp2px(2f)
        }
        binding.tvProgressTime.visibility = View.GONE
        mStartSeek = false
    }

    override fun onProgress(bundle: Bundle?) {
        bundle?.let { handlePlayProgress(it) }
    }

    override fun onSpeed(speed: Int?) {
        if (AppConfig.isDebug()) {
            binding.speedTv.text = getString(R.string.speed_kbps, speed)
        }
    }

    override fun onRcvFirstFrame() {
        Logger.i("[onPrepared in TXVideoBaseView]")
        mPlayerActivity.setVideoLoadState(false)
        if (isVisible.not()) pausePlayer()
        //binding.coverIv.visibility = View.GONE
    }

    override fun onPlaying() {
        if (mManualPause) {
            mManualPause = false
            binding.pauseIv.setImageResource(R.drawable.player_suspend)
        }
    }

    override fun onLoading(isLoading: Boolean) {
        if (isVisible.not()) pausePlayer()
        if (isLoading && mStartSeek.not()) {
            binding.seekbarLav.playAnimation()
            binding.seekbarLav.visibility = View.VISIBLE
        } else {
            binding.seekbarLav.visibility = View.GONE
            binding.seekbarLav.cancelAnimation()
        }
    }

    override fun onLoadFail(event: Int, failInfo: String) {
        if (event == TXVodConstants.VOD_PLAY_ERR_DECODE_SUBTITLE_FAIL || event == TXVodConstants.VOD_PLAY_EVT_SELECT_TRACK_COMPLETE) {
            stopPlayer()
            startPlay()
        }

        if (event == TXLiveConstants.ERR_LICENSE_CHECK_FAIL) {
            TXCSDKService.init(requireContext())
            stopPlayer()
            startPlay()
        }

        val content =
            "movieCode:${mVideoModel.movieCode},ep:${mVideoModel.ep},url:${mVideoModel.url},srtUrl:${mVideoModel.srtUrl},failInfo:$failInfo"

        EasyHttp.post(this).api(ClientReportLogApi(content, "video_play"))
            .request(object : OnHttpListener<HttpData<Void>> {
                override fun onHttpSuccess(result: HttpData<Void>?) {}
                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    override fun onGetPlayInfoFail() {
        Logger.e("onGetPlayInfoFail.getPlayInfoFailCount:$getPlayInfoFailCount")
        if (getPlayInfoFailCount == 3) return
        val url = if (mPlayerActivity.lang == null) EP_DETAIL_URL else AD_EP_DETAIL_URL
        EasyHttp.get(this).api(
            EpisodesDetailApi(
                mVideoModel.movieId, mVideoModel.ep, mPlayerActivity.lang
            ).setUrl(url)
        ).request(object : OnHttpListener<HttpData<VideoModel>> {
            override fun onHttpSuccess(result: HttpData<VideoModel>?) {
                result?.getData()?.let { videoModel ->
                    mVideoModel = videoModel
                    mTXVodPlayerWrapper.setVideoModel(videoModel)
                    if (mPlayerActivity.mCurrentEpisode == videoModel.ep) {
                        Logger.e("onLoadFail.重新播放")
                        startPlay()
                    }
                }
                getPlayInfoFailCount++
            }

            override fun onHttpFail(throwable: Throwable?) {
                if (mVideoModel.ep != 0) toast(throwable?.message)

            }
        })
    }

    override fun onLoadEnd() {
        val activityStatus = mPlayerActivity.mMovieDetailBean?.activity?.activityStatus ?: -1
        val actualTotalEpisode =
            if (mPlayerActivity.actualTotalEpisode == 1 && activityStatus == 0) 0 else mPlayerActivity.actualTotalEpisode
        if (mVideoModel.ep == actualTotalEpisode) {
            pausePlayer()
            mPlayerActivity.backInterception(true)
        } else {
            //播放结束跳转下一集,因为外部adapter下标未0，ep为1，所以不对ep操作可以跳转下一集
            EventBus.getDefault().post(SelectEpisodesNotify(mVideoModel.ep, true))
        }
    }

}