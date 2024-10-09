package com.tt.dramatime.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import com.flyjingfish.android_aop_core.annotations.CheckNetwork
import com.hjq.bar.TitleBar
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tt.dramatime.R
import com.tt.dramatime.aop.Log
import com.tt.dramatime.app.AppActivity
import com.tt.dramatime.widget.BrowserView
import com.tt.dramatime.widget.BrowserView.BrowserChromeClient
import com.tt.dramatime.widget.BrowserView.BrowserViewClient
import com.tt.dramatime.widget.StatusLayout
import com.tt.dramatime.widget.StatusLayout.OnRetryListener

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 浏览器界面
 */
class BrowserActivity : AppActivity(), com.tt.dramatime.action.StatusAction, OnRefreshListener {

    companion object {

        const val INTENT_KEY_IN_URL: String = "url"

        @CheckNetwork
        @Log
        fun start(context: Context, url: String) {
            if (TextUtils.isEmpty(url)) {
                return
            }
            val intent = Intent(context, BrowserActivity::class.java)
            intent.putExtra(INTENT_KEY_IN_URL, url)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val hintLayout: StatusLayout? by lazy { findViewById(R.id.hl_browser_hint) }
    private val progressBar: ProgressBar? by lazy { findViewById(R.id.pb_browser_progress) }
    private val refreshLayout: SmartRefreshLayout? by lazy { findViewById(R.id.sl_browser_refresh) }
    private val browserView: BrowserView? by lazy { findViewById(R.id.wv_browser_view) }

    override fun getLayoutId(): Int {
        return R.layout.browser_activity
    }

    override fun initView() {
        // 设置 WebView 生命管控
        browserView?.setLifecycleOwner(this)
        // 设置网页刷新监听
        refreshLayout?.setOnRefreshListener(this)
        refreshLayout?.setEnableRefresh(false)
    }

    override fun initData() {
        showDialog()
        browserView?.apply {
            setBrowserViewClient(AppBrowserViewClient())
            setBrowserChromeClient(AppBrowserChromeClient(this))
            loadUrl(getString(INTENT_KEY_IN_URL)!!)
        }
    }

    override fun getStatusLayout(): StatusLayout? {
        return hintLayout
    }

    override fun onLeftClick(titleBar: TitleBar?) {
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        browserView?.apply {
            if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
                // 后退网页并且拦截该事件
                goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 重新加载当前页
     */
    @CheckNetwork
    private fun reload() {
        browserView?.reload()
    }

    /**
     * [OnRefreshListener]
     */
    override fun onRefresh(refreshLayout: RefreshLayout) {
        reload()
    }

    private inner class AppBrowserViewClient : BrowserViewClient() {

        /**
         * 网页加载错误时回调，这个方法会在 onPageFinished 之前调用
         */
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            // 这里为什么要用延迟呢？因为加载出错之后会先调用 onReceivedError 再调用 onPageFinished
            post {
                showError(object : OnRetryListener {
                    override fun onRetry(layout: StatusLayout) {
                        reload()
                    }
                })
            }
        }

        /**
         * 开始加载网页
         */
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            progressBar?.visibility = View.VISIBLE
        }

        /**
         * 完成加载网页
         */
        override fun onPageFinished(view: WebView, url: String) {
            progressBar?.visibility = View.GONE
            refreshLayout?.finishRefresh()
            hideDialog()
        }
    }

    private inner class AppBrowserChromeClient constructor(view: BrowserView) : BrowserChromeClient(view) {

        /**
         * 收到网页标题
         */
        override fun onReceivedTitle(view: WebView, title: String?) {
            if (title == null) {
                return
            }
            setTitle(title)
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
            if (icon == null) {
                return
            }
            //setRightIcon(BitmapDrawable(resources, icon))
        }

        /**
         * 收到加载进度变化
         */
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            progressBar?.progress = newProgress
        }
    }
}