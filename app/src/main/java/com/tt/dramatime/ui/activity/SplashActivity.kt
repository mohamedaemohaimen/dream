package com.tt.dramatime.ui.activity

import android.content.Intent
import android.view.View
import com.facebook.appevents.AppEventsLogger
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tt.dramatime.R
import com.tt.dramatime.app.AppApplication
import com.tt.dramatime.app.AppApplication.Companion.appContext
import com.tt.dramatime.app.AppConstant.UUID_LOGIN
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.SplashActivityBinding
import com.tt.dramatime.http.api.CommonAdConfigApi
import com.tt.dramatime.http.api.UpdateTokenApi
import com.tt.dramatime.http.api.UuidLoginApi
import com.tt.dramatime.http.db.CommonAdConfigHelper
import com.tt.dramatime.http.db.MMKVDurableConstant
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_ALREADY_OPEN
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.MMKVUserConstant.Companion.KEY_AUTHORIZATION
import com.tt.dramatime.http.db.MMKVUserConstant.Companion.KEY_LOGIN_SOURCE
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.google.ad.AppOpenAdManager
import com.tt.dramatime.manager.player.TXCSDKService
import com.tt.dramatime.other.AppConfig
import java.util.Locale


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 闪屏界面
 * </pre>
 */
class SplashActivity :
    BaseViewBindingActivity<SplashActivityBinding>({ SplashActivityBinding.inflate(it) }) {

    var count = 0

    var loginStatus = 0

    private val enterRunnable = Runnable {
        // 停止自动显示，避免进入主页后自动展示广告打断用户行为
        (application as AppApplication).appOpenAdManager?.stopAutoShow()
        startHomeActivity()
    }

    override fun initActivity() {
        // 问题及方案：https://www.cnblogs.com/net168/p/5722752.html
        // 如果当前 Activity 不是任务栈中的第一个 Activity
        if (!isTaskRoot) {
            val intent: Intent? = intent
            // 如果当前 Activity 是通过桌面图标启动进入的
            if (intent != null && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
                // 对当前 Activity 执行销毁操作，避免重复实例化入口
                finish()
                return
            }
        }
        super.initActivity()
    }

    override fun initView() {
        binding.ivSplashDebug.let {
            it.setText(AppConfig.getBuildType().uppercase(Locale.getDefault()))
            if (AppConfig.isDebug()) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.INVISIBLE
            }
        }
    }

    override fun initData() {
        TXCSDKService.init(appContext)

        if (MMKVExt.getDurableMMKV()?.decodeBool(KEY_ALREADY_OPEN) == false) {
            AppEventsLogger.newLogger(this).logEvent("fb_first_open")
        }

        if (MMKVExt.getUserMmkv()?.getString(KEY_AUTHORIZATION, "")
                .isNullOrEmpty() || MMKVExt.getDurableMMKV()?.decodeBool(KEY_ALREADY_OPEN) == false
        ) {
            uuidLogin()
        } else {
            // 刷新用户信息
            updateToken()
        }
    }

    @SingleClick(3000)
    private fun uuidLogin() {
        EasyHttp.post(this).api(UuidLoginApi(AppConfig.getWidevineID(getContext())))
            .request(object : OnHttpListener<HttpData<UuidLoginApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UuidLoginApi.Bean?>) {
                    notifyAuthLanguage(data)
                    MMKVExt.getUserMmkv()?.encode(KEY_LOGIN_SOURCE, UUID_LOGIN)
                    loginStatus = 1
                    showAppOpenAd()
                }

                override fun onHttpFail(throwable: Throwable?) {
                    loginStatus =
                        if (throwable?.message == getString(R.string.http_repeat_submit)) 1 else 2
                    showAppOpenAd()
                }
            })
    }

    @SingleClick(3000)
    private fun updateToken() {
        EasyHttp.post(this).api(UpdateTokenApi())
            .request(object : OnHttpListener<HttpData<UuidLoginApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UuidLoginApi.Bean?>) {
                    notifyAuthLanguage(data)
                    loginStatus = 1
                    showAppOpenAd()
                }

                override fun onHttpFail(throwable: Throwable?) {
                    loginStatus =
                        if (throwable?.message == getString(R.string.http_repeat_submit)) 1 else 2
                    showAppOpenAd()
                }
            })
    }

    /**更新token与内容语言*/
    private fun notifyAuthLanguage(data: HttpData<UuidLoginApi.Bean?>) {
        val authorization = "Bearer ${data.getData()?.accessToken}"
        // 更新 Authorization
        MMKVExt.getUserMmkv()?.putString(KEY_AUTHORIZATION, authorization)
        data.getData()?.contentLanguage?.let { contentLanguage ->
            MMKVExt.getDurableMMKV()
                ?.encode(MMKVDurableConstant.KEY_CONTENT_LANGUAGE, contentLanguage)
        }
    }

    //屏蔽IAA模块代码 showAppOpenAd替换为startHomeActivity
    private fun showAppOpenAd() {
        EasyHttp.get(this).api(CommonAdConfigApi())
            .request(object : OnHttpListener<HttpData<List<CommonAdConfigApi.Bean>>> {
                override fun onHttpSuccess(result: HttpData<List<CommonAdConfigApi.Bean>>?) {
                    result?.getData()?.let {
                        CommonAdConfigHelper.setConfig(it)
                        if (CommonAdConfigHelper.getOpenAdEnableStatus()) {
                            (application as AppApplication).appOpenAdManager?.showAppOpenAd(
                                this@SplashActivity,
                                object : AppOpenAdManager.AppOpenAdShowCallback {
                                    override fun onAppOpenAdShow() {
                                        // 开屏广告已显示，停止计时线程
                                        removeCallbacks(enterRunnable)
                                    }

                                    override fun onAppOpenAdShowComplete() {
                                        // 开屏广告播放完毕（成功或失败），停止计时线程并进入主页
                                        removeCallbacks(enterRunnable)
                                        startHomeActivity()
                                    }
                                },
                                true
                            )
                            // 三秒内没有显示出广告，自动进入主页
                            postDelayed(enterRunnable, 3000)
                        } else {
                            startHomeActivity()
                        }
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    startHomeActivity()
                }
            })
    }

    private fun startHomeActivity() {
        HomeActivity.start(this@SplashActivity, loginStatus = loginStatus)
        finish()
    }

    override fun createStatusBarConfig(): ImmersionBar {
        // 隐藏状态栏和导航栏
        return super.createStatusBarConfig().hideBar(BarHide.FLAG_HIDE_BAR)
    }

    /*override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }*/

}