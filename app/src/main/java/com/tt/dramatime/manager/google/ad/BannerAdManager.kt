package com.tt.dramatime.manager.google.ad

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.orhanobut.logger.Logger

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/8/15 下午4:55
 *   Desc : Banner广告管理
 * </pre>
 */
class BannerAdManager {

    private val bannerListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            // 广告加载成功
            bannerAdLoadCallback?.invoke(true)
            Logger.d("BannerAdManager.广告加载成功")
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            super.onAdFailedToLoad(loadAdError)
            // 广告加载失败
            bannerAdLoadCallback?.invoke(false)
            Logger.e("BannerAdManager.广告加载失败:${loadAdError.message}")
        }

        override fun onAdImpression() {
            super.onAdImpression()
            // 被记录为展示成功时调用
        }

        override fun onAdClicked() {
            super.onAdClicked()
            // 被点击时调用
        }

        override fun onAdOpened() {
            super.onAdOpened()
            // 广告落地页打开时调用
        }

        override fun onAdClosed() {
            super.onAdClosed()
            // 广告落地页关闭时调用
        }
    }

    private var bannerAdLoadCallback: ((succeed: Boolean) -> Unit)? = null


    fun createBannerAdView(bannerAdView: AdView) {
        // 获取页面的根布局
        bannerAdView.apply {
            // 设置Banner的尺寸
            setAdSize(AdSize(236, 64))
            //测试广告 ca-app-pub-3940256099942544/6300978111
            //正式广告 ca-app-pub-3832968711572961/2824966399
            adUnitId = "ca-app-pub-3832968711572961/2824966399"
            // 设置广告事件回调
            adListener = bannerListener
            // 加载广告
            loadAd(AdRequest.Builder().build())
        }
    }

}