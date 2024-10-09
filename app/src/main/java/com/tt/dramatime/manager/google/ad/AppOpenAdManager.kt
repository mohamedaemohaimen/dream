package com.tt.dramatime.manager.google.ad

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.orhanobut.logger.Logger
import com.tt.dramatime.app.AppApplication

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/8/14 下午3:54
 *   Desc : App开屏广告管理类
 * </pre>
 */
class AppOpenAdManager {

    //DramaTime ca-app-pub-3832968711572961/3336333797
    //测试 ca-app-pub-3940256099942544/9257395921
    private val appOpenAdUnit = "ca-app-pub-3832968711572961/3336333797"
    private val appOpenAdValidTime = 4 * 60 * 60 * 1000

    private var currentAppOpenAd: AppOpenAd? = null
    private var currentAppOpenAdLoadedTime: Long = 0

    private var loadingAd: Boolean = false
    var showingAd: Boolean = false
        private set

    private var showAdWhenReady: Boolean = false
    private var tempActivity: Activity? = null
    private var tempAppOpenAdShowCallback: AppOpenAdShowCallback? = null

    private val appOpenAdLoadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
        override fun onAdLoaded(appOpenAd: AppOpenAd) {
            super.onAdLoaded(appOpenAd)
            Logger.d("onAdLoaded: ${appOpenAd.responseInfo}")
            // 开屏广告加载成功
            currentAppOpenAdLoadedTime = System.currentTimeMillis()
            currentAppOpenAd = appOpenAd
            loadingAd = false
            // 设置了当加载完成时打开广告
            if (showAdWhenReady) {
                showAdWhenReady = false
                tempActivity?.let {
                    showAppOpenAd(it, tempAppOpenAdShowCallback)
                }
            }
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            super.onAdFailedToLoad(loadAdError)
            Logger.e("onAdFailedToLoad. message: ${loadAdError.message} code: ${loadAdError.code} responseInfo: ${loadAdError.responseInfo}")
            // 开屏广告加载失败
            // 官方不建议在此回调中重新加载广告
            // 如果确实需要，则必须限制最大重试次数，避免在网络受限的情况下连续多次请求
            loadingAd = false
        }
    }

    fun loadAd() {
        if (!loadingAd) {
            loadingAd = true
            AppOpenAd.load(AppApplication.appContext, appOpenAdUnit, AdRequest.Builder().build(), appOpenAdLoadCallback)
        }
    }

    fun showAppOpenAd(activity: Activity, appOpenAdShowCallback: AppOpenAdShowCallback? = null, showAdWhenReady: Boolean = false) {
        if (showingAd) {
            // 开屏广告正在展示
            return
        }
        if (!appOpenAdAvailable()) {
            // 开屏广告不可用，重新加载
            loadAd()
            if (showAdWhenReady) {
                // 设置当加载完成时打开广告，缓存当前页面和回调方法
                this.showAdWhenReady = true
                tempActivity = activity
                tempAppOpenAdShowCallback = appOpenAdShowCallback
            } else {
                // 广告不可用，回调播放完成继续执行后续步骤
                appOpenAdShowCallback?.onAppOpenAdShowComplete()
            }
            return
        }
        currentAppOpenAd?.run {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    // 广告展示
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    // 广告展示失败，清空缓存数据，重新加载
                    showingAd = false
                    currentAppOpenAd = null
                    appOpenAdShowCallback?.onAppOpenAdShowComplete()
                    tempActivity = null
                    tempAppOpenAdShowCallback = null
                    loadAd()
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    // 广告关闭，清空缓存数据，重新加载
                    showingAd = false
                    currentAppOpenAd = null
                    appOpenAdShowCallback?.onAppOpenAdShowComplete()
                    tempActivity = null
                    tempAppOpenAdShowCallback = null
                    loadAd()
                }
            }
            showingAd = true
            show(activity)
            appOpenAdShowCallback?.onAppOpenAdShow()
        }
    }

    fun stopAutoShow() {
        showAdWhenReady = false
        tempActivity = null
        tempAppOpenAdShowCallback = null
    }

    private fun appOpenAdAvailable(): Boolean {
        return currentAppOpenAd != null && (currentAppOpenAdLoadedTime != 0L && System.currentTimeMillis() - currentAppOpenAdLoadedTime <= appOpenAdValidTime)
    }

    interface AppOpenAdShowCallback {

        fun onAppOpenAdShow()

        fun onAppOpenAdShowComplete()
    }
}
