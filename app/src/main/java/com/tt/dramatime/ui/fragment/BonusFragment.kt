package com.tt.dramatime.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.NotificationUtils.areNotificationsEnabled
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.tt.base.BaseDialog
import com.tt.dramatime.R
import com.tt.dramatime.app.AppActivity
import com.tt.dramatime.app.AppConstant.thirdLoginIntegral
import com.tt.dramatime.app.BaseViewBindTitleBarFragment
import com.tt.dramatime.databinding.BonusFragmentBinding
import com.tt.dramatime.http.api.AccomplishTaskApi
import com.tt.dramatime.http.api.AdTriggerApi
import com.tt.dramatime.http.api.SignApi
import com.tt.dramatime.http.api.SignTaskApi
import com.tt.dramatime.http.api.TaskListApi
import com.tt.dramatime.http.api.TaskReceiveApi
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.ActivityManager
import com.tt.dramatime.other.AppConfig
import com.tt.dramatime.ui.activity.HomeActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.activity.task.EmailActivity
import com.tt.dramatime.ui.adapter.bonus.BonusCheckInAdapter
import com.tt.dramatime.ui.adapter.bonus.BonusTaskAdapter
import com.tt.dramatime.ui.dialog.bonus.CheckInSuccessfulDialog
import com.tt.dramatime.ui.dialog.login.LoginDialogFragment
import com.tt.dramatime.util.StartNotificationUtils.getNotificationIntent

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 任务 Fragment
 * </pre>
 */
class BonusFragment : BaseViewBindTitleBarFragment<BonusFragmentBinding, AppActivity>({
    BonusFragmentBinding.inflate(it)
}) {

    companion object {
        fun newInstance(): BonusFragment {
            val fragment = BonusFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    private val mBonusCheckInAdapter by lazy {
        BonusCheckInAdapter(mutableListOf())
    }

    private val mBonusTaskAdapter by lazy {
        BonusTaskAdapter(mutableListOf())
    }

    private var mSignDay: Int? = null

    private var bonus = 0

    private var mTaskId: Long? = null
    private var mBusinessType: Int? = null
    private var mTaskBonus: Int? = null
    private var mTaskPosition = 0
    private var mChildTaskPosition = 0

    private var mMovieAdTaskId: Long? = null
    private var mMovieAdTaskBonus: Int? = null
    private var mMovieAdChildTaskPosition = 0

    private var pushEnable: Boolean? = null

    private var mSignStatus: Int? = null
    private var noCheckIn = false

    private var mWatchingStatus = false
    private var mWatchingAd = false
    private var traceId = ""
    private var watchAdCount = 1

    private var isLoad = false
    private var isLoading = false

    private var mMovieAdBean: TaskListApi.Bean? = null

    private var mAdMovieCompletionStatus = true

    override fun isStatusBarEnabled(): Boolean {
        return !super.isStatusBarEnabled()
    }

    override fun initView() {
        binding.checkInRv.adapter = mBonusCheckInAdapter
        binding.tasksRv.adapter = mBonusTaskAdapter

        setOnClickListener(binding.checkInBtn, binding.operationBtn)

        mBonusTaskAdapter.addOnItemChildClickListener(R.id.claim_ll) { _, _, position ->
            taskClick(position)
        }
    }

    @SingleClick(1000)
    private fun taskClick(position: Int) {
        mBonusTaskAdapter.getItem(position)?.apply {
            var mCompletionStatus = true
            mBusinessType = businessType
            mTaskPosition = position
            when (businessType) {
                2 -> {
                    list?.forEachIndexed { index, listBean ->
                        if (listBean.status == 0 && mCompletionStatus) {
                            mTaskId = listBean.id
                            mTaskBonus = listBean.integral
                            mChildTaskPosition = index
                            mCompletionStatus = false
                            return@forEachIndexed
                        }
                    }
                }

                9 -> {
                    mCompletionStatus = false
                    list?.forEachIndexed { index, listBean ->
                        if (listBean.status == 1 && mCompletionStatus.not()) {
                            mTaskId = listBean.id
                            mTaskBonus = listBean.integral
                            mChildTaskPosition = index
                            mCompletionStatus = true
                            return@forEachIndexed
                        }
                    }
                }

                else -> {
                    mTaskId = task?.id
                    mTaskBonus = task?.integral
                    mCompletionStatus = task?.status == 1
                }
            }

            if (mCompletionStatus) {
                receiveBonus()
            } else {
                //1 签到任务 2 看广告任务 3开启通知 4App评分任务 7 第三方登录绑定  8绑定邮箱  9观看剧集时长任务
                when (businessType) {
                    2 -> loadAd()
                    3 -> startActivity(getNotificationIntent(requireActivity().packageName))
                    4 -> startInAppReview()
                    7 -> LoginDialogFragment().show(
                        parentFragmentManager, LoginDialogFragment.TAG
                    )

                    8 -> EmailActivity.start(requireContext())
                    9 -> {
                        if (ActivityManager.getInstance()
                                .getActivity(PlayerActivity::class.java) != null
                        ) finish() else HomeActivity.start(requireContext())
                    }
                }
            }

        }
    }

    @SingleClick(1000)
    override fun onClick(view: View) {
        when (view) {
            binding.checkInBtn -> checkIn()
            binding.operationBtn -> {
                binding.operationBtn.visibility = View.GONE
                getBonusTaskList()
            }
        }
    }

    override fun initData() {}

    override fun onResume() {
        super.onResume()
        binding.bonusesTv.text = UserProfileHelper.getBonus().toString()
        if (mWatchingAd) mWatchingAd = false else getBonusTaskList()
    }

    fun getBonusTaskList(autoSign: Boolean = true) {
        if (isLoading) return
        bonus = UserProfileHelper.getBonus()
        pushEnable = areNotificationsEnabled()

        noCheckIn = false

        if (isLoad.not()) {
            binding.movieLoadingLav.playAnimation()
            binding.movieLoadingLav.visibility = View.VISIBLE
        }

        isLoading = true

        EasyHttp.get(this@BonusFragment).api(SignTaskApi())
            .request(object : OnHttpListener<HttpData<SignTaskApi.Bean>?> {
                override fun onHttpSuccess(signTaskResult: HttpData<SignTaskApi.Bean>?) {
                    getTaskList(signTaskResult, autoSign)
                }

                override fun onHttpFail(throwable: Throwable?) {
                    binding.movieLoadingLav.visibility = View.GONE
                    binding.movieLoadingLav.cancelAnimation()
                    isLoading = false
                    if (isLoad.not()) {
                        binding.operationBtn.visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun signTaskResult(signTaskResult: SignTaskApi.Bean, autoSign: Boolean) {
        signTaskResult.apply {
            mSignDay = signDay
            binding.checkInDayTv.text = mSignDay.toString()

            mBonusCheckInAdapter.submitList(list)

            list?.forEach {
                //状态 0 不可签到 1 已签到 2 可签到
                if (it.status == 2) {
                    noCheckIn = true
                    binding.checkInTv.text = getString(R.string.check_in)
                    binding.reminderIv.visibility = View.GONE
                    setCheckInBtnBackground(R.color.color_23E1FF, R.color.color_C640FF)
                    if (autoSign) sign()
                    return@forEach
                }
            }

            if (noCheckIn.not()) {
                checkInButtonStatus()
            }
        }
    }

    private fun checkInButtonStatus() {

        if (isAdded.not()) return

        mAdMovieCompletionStatus = true

        mMovieAdBean?.list?.forEachIndexed { index, listBean ->
            if (listBean.status == 0 && mAdMovieCompletionStatus) {
                mMovieAdTaskId = listBean.id
                mMovieAdTaskBonus = listBean.integral
                mMovieAdChildTaskPosition = index
                mAdMovieCompletionStatus = false
                return@forEachIndexed
            }
        }

        if (mAdMovieCompletionStatus) {
            if (areNotificationsEnabled()) {
                binding.checkInTv.text = getString(R.string.come_back_tomorrow)
                binding.reminderIv.visibility = View.GONE
                setCheckInBtnBackground(
                    R.color.color_23E1FF_50, R.color.color_C640FF_50
                )
            } else {
                binding.checkInTv.text = getString(R.string.remind_me_next_time)
                binding.reminderIv.visibility = View.VISIBLE
                binding.reminderIv.setBackgroundResource(R.drawable.bonus_reminder_ic)
                setCheckInBtnBackground(R.color.color_23E1FF, R.color.color_C640FF)
            }
        } else {
            binding.checkInTv.text = getString(R.string.extra_bonus, mMovieAdTaskBonus)
            binding.reminderIv.visibility = View.VISIBLE
            binding.reminderIv.setBackgroundResource(R.drawable.bonus_ad_ic)
            setCheckInBtnBackground(R.color.color_23E1FF, R.color.color_C640FF)
        }
    }

    private fun getTaskList(signTaskResult: HttpData<SignTaskApi.Bean>?, autoSign: Boolean) {
        EasyHttp.get(this@BonusFragment).api(TaskListApi())
            .request(object : OnHttpListener<HttpData<List<TaskListApi.Bean>>?> {
                override fun onHttpSuccess(taskListResult: HttpData<List<TaskListApi.Bean>>?) {
                    binding.operationBtn.visibility = View.GONE
                    binding.movieLoadingLav.visibility = View.GONE
                    binding.movieLoadingLav.cancelAnimation()

                    isLoad = true
                    isLoading = false

                    signTaskResult?.getData()?.apply {
                        binding.checkInGp.visibility = View.VISIBLE
                        val list = taskListResult?.getData()?.filter { it.businessType == 10 }
                        if (list?.isNotEmpty() == true) {
                            mMovieAdBean = list[0]
                        }
                        signTaskResult(this, autoSign)
                    }

                    taskListResult?.getData()?.apply {
                        binding.taskGp.visibility = View.VISIBLE
                        val list = this.filter { it.businessType != 10 }

                        mBonusTaskAdapter.submitList(list)
                        setThirdLoginIntegral()
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    binding.movieLoadingLav.visibility = View.GONE
                    binding.movieLoadingLav.cancelAnimation()
                    isLoading = false
                    if (isLoad.not()) {
                        binding.operationBtn.visibility = View.VISIBLE
                    }
                }
            })
    }

    fun setEmptyDataView() {
        mBonusCheckInAdapter.submitList(mutableListOf())
        mBonusTaskAdapter.submitList(mutableListOf())
        binding.operationBtn.visibility = View.VISIBLE
    }

    fun refreshNotificationsEnabledStatus() {
        if (pushEnable == false && areNotificationsEnabled()) {
            pushEnable = true
            getTaskList()
        }
    }

    private fun checkIn() {
        if (noCheckIn) {
            showDialog()
            sign()
        } else if (mAdMovieCompletionStatus.not()) {
            mBusinessType = 10
            mTaskId = mMovieAdTaskId
            mTaskBonus = mMovieAdTaskBonus
            loadAd()
        } else if (areNotificationsEnabled().not()) {
            startActivity(getNotificationIntent(requireActivity().packageName))
        }
    }

    private fun sign() {
        if (noCheckIn) {
            mSignDay?.let {
                EasyHttp.post(this).api(SignApi(it.plus(1)))
                    .request(object : OnHttpListener<HttpData<SignApi.Bean>> {
                        override fun onHttpSuccess(result: HttpData<SignApi.Bean>?) {
                            hideDialog()
                            mSignDay = it.plus(1)
                            mSignStatus = 1
                            noCheckIn = false
                            binding.checkInDayTv.text = mSignDay.toString()
                            UserProfileHelper.setSignStatus(1)

                            mBonusCheckInAdapter.getItem(it)?.apply {
                                status = 1
                                mBonusCheckInAdapter.notifyItemChanged(it)
                                CheckInSuccessfulDialog.Builder(
                                    requireContext(), integral, mMovieAdBean
                                ).setListener(object : CheckInSuccessfulDialog.OnListener {
                                    override fun onConfirm(dialog: BaseDialog?) {
                                        checkIn()
                                    }
                                }).show()
                                bonus += integral
                                binding.bonusesTv.text = bonus.toString()
                                UserProfileHelper.setBonus(bonus)
                            }
                            val activity =
                                ActivityManager.getInstance().getActivity(HomeActivity::class.java)

                            activity?.let { mHomeActivity ->
                                (mHomeActivity as HomeActivity).navigationAdapterNotify()
                            }

                            checkInButtonStatus()
                        }

                        override fun onHttpFail(throwable: Throwable?) {
                            hideDialog()
                            toast(throwable?.message)
                        }
                    })
            }
        }
    }

    /**设置签到按钮背景*/
    private fun setCheckInBtnBackground(startColor: Int, endColor: Int) {
        binding.checkInBtn.shapeDrawableBuilder.setRadius(dp2px(42f).toFloat())
            .setSolidGradientColors(
                ContextCompat.getColor(requireContext(), startColor),
                ContextCompat.getColor(requireContext(), endColor)
            ).intoBackground()
    }

    private fun accomplishTask() {
        if (mTaskId == null || mBusinessType == null) return
        EasyHttp.post(this).api(AccomplishTaskApi(mTaskId!!, mBusinessType!!))
            .request(object : HttpCallbackProxy<HttpData<AccomplishTaskApi.Bean>>(this) {
                override fun onHttpSuccess(result: HttpData<AccomplishTaskApi.Bean>?) {
                    if (mBusinessType == 10) {
                        mMovieAdBean?.apply {
                            list?.get(mMovieAdChildTaskPosition)?.status = 2
                            checkInButtonStatus()
                            setBonus(mTaskBonus)
                            finishNum += 1
                        }
                    } else {
                        mBonusTaskAdapter.getItem(mTaskPosition)?.apply {
                            if (mBusinessType == 2) {
                                list?.get(mChildTaskPosition)?.status = 2
                                setBonus()
                            } else {
                                task?.status = 1
                            }
                            finishNum += 1
                            mBonusTaskAdapter.notifyItemChanged(mTaskPosition)
                        }
                    }
                }
            })
    }

    private fun receiveBonus() {
        if (mTaskId == null || mBusinessType == null || mTaskBonus == null) return
        EasyHttp.post(this).api(TaskReceiveApi(mTaskId!!, mBusinessType!!))
            .request(object : HttpCallbackProxy<HttpData<TaskReceiveApi.Bean>>(this) {
                override fun onHttpSuccess(result: HttpData<TaskReceiveApi.Bean>?) {
                    if (mBusinessType == 9) {
                        getTaskList()
                    } else {
                        mBonusTaskAdapter.getItem(mTaskPosition)?.task?.status = 2
                        if (mBonusTaskAdapter.getItem(mTaskPosition)?.type == 1) {
                            mBonusTaskAdapter.removeAt(mTaskPosition)
                        } else {
                            mBonusTaskAdapter.notifyItemChanged(mTaskPosition)
                        }
                    }

                    setBonus(result?.getData()?.total)
                }
            })
    }

    private fun setBonus(receiveBonus: Int? = 0) {
        if (!isAdded) return
        val mBonus = if (receiveBonus != null && receiveBonus > 0) receiveBonus else mTaskBonus
        toastBonus(getString(R.string.add_coins, mBonus))
        bonus += mBonus!!
        binding.bonusesTv.text = bonus.toString()
        UserProfileHelper.setBonus(bonus)
    }

    private fun getTaskList() {
        EasyHttp.get(this).api(TaskListApi())
            .request(object : OnHttpListener<HttpData<List<TaskListApi.Bean>>> {
                override fun onHttpSuccess(result: HttpData<List<TaskListApi.Bean>>?) {
                    result?.getData()?.apply {
                        mBonusTaskAdapter.submitList(this)
                        setThirdLoginIntegral()
                    }
                }

                override fun onHttpFail(p0: Throwable?) {}
            })
    }

    private fun List<TaskListApi.Bean>.setThirdLoginIntegral() {
        if (thirdLoginIntegral == null) {
            this.forEach {
                if (it.businessType == 7) {
                    thirdLoginIntegral = it.task?.integral
                    return@forEach
                }
            }
        }
    }

    /**
     * 广告观看上报
     */
    private fun adTrigger(adUnitId: String, watchingStatus: Boolean) {
        EasyHttp.post(this).api(
            AdTriggerApi(adUnitId, if (watchingStatus) watchAdCount else -1, null, null, traceId, 0)
        ).request(object : OnHttpListener<HttpData<AdTriggerApi.Bean>> {
            override fun onHttpSuccess(result: HttpData<AdTriggerApi.Bean>?) {
                result?.getData()?.let {
                    traceId = it.traceId.toString()
                    watchAdCount++
                    if (watchingStatus) accomplishTask()
                }
            }

            override fun onHttpFail(throwable: Throwable?) {}
        })
        mWatchingStatus = false
    }

    private fun loadAd() {
        showDialog(getString(R.string.video_is_loading))

        val adRequest = AdRequest.Builder().build()
        //测试的广告id ca-app-pub-3940256099942544/5224354917
        val adUnitId =
            if (AppConfig.isDebug()) "ca-app-pub-3940256099942544/5224354917" else "ca-app-pub-3832968711572961/5766702420"

        RewardedAd.load(requireContext(), adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Logger.d(adError.toString())
                hideDialog()
                toast(R.string.ad_load_failed)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Logger.d("Ad was loaded.")
                hideDialog()
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Logger.d("fullScreenContentCallback.Ad was clicked.")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        adTrigger(ad.adUnitId, mWatchingStatus)
                        Logger.d("fullScreenContentCallback.Ad dismissed fullscreen content.")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // Called when ad fails to show.
                        Logger.e("fullScreenContentCallback.Ad failed to show fullscreen content.")
                    }

                    override fun onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Logger.d("fullScreenContentCallback.Ad recorded an impression.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Logger.d("fullScreenContentCallback.Ad showed fullscreen content.")
                    }
                }

                ad.show(requireActivity()) { rewardItem ->
                    // Handle the reward.
                    mWatchingStatus = true
                    mWatchingAd = true
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Logger.d("User earned the reward.rewardAmount:$rewardAmount rewardType:$rewardType")
                }
            }
        })
    }

    private fun startInAppReview() {
        val reviewManager: ReviewManager = ReviewManagerFactory.create(requireContext())
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo: ReviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener { reviewFlow ->
                    Logger.e("addOnCompleteListener.result:${reviewFlow.result} isComplete:${reviewFlow.isComplete} isSuccessful:${reviewFlow.isSuccessful}")
                    if (reviewFlow.isComplete) {
                        accomplishTask()
                    }
                }
            } else {
                // There was some problem, log or handle the error code.
                val exception = task.exception
                Logger.e("addOnCompleteListener.error:${exception}")
            }
        }
    }

}