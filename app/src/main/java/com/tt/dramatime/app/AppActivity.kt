package com.tt.dramatime.app

import android.content.Context
import androidx.annotation.StringRes
import com.gyf.immersionbar.ImmersionBar
import com.hjq.bar.TitleBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.hjq.language.MultiLanguages
import com.tt.base.BaseActivity
import com.tt.dramatime.R
import com.tt.dramatime.action.TitleBarAction
import com.tt.dramatime.action.ToastAction
import com.tt.dramatime.http.api.ClickMessageApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.activity.HomeActivity
import com.tt.dramatime.ui.dialog.WaitDialog


/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 * time   : 2018/10/18
 * desc   : Activity 业务基类
 */
abstract class AppActivity : BaseActivity(), ToastAction, TitleBarAction {

    companion object {
        const val KEY_MESSAGE_ID = "messageId"
    }

    /** 标题栏对象 */
    private var titleBar: TitleBar? = null

    /** 状态栏沉浸 */
    private var immersionBar: ImmersionBar? = null

    /** 加载对话框 */
    private var dialog: WaitDialog.Builder? = null

    /** 对话框数量 */
    private var dialogCount: Int = 0

    /**
     * 当前加载对话框是否在显示中
     */
    open fun isShowDialog(): Boolean {
        return dialog != null && dialog?.getDialog()?.isShowing == true
    }

    /**
     * 显示加载对话框
     */
    open fun showDialog(
        message: String? = "", delayMillis: Long = 300, automaticShutdown: Boolean = false
    ) {
        if (isFinishing || isDestroyed) {
            return
        }
        dialogCount++
        postDelayed(Runnable {
            if ((dialogCount <= 0) || isFinishing || isDestroyed) {
                return@Runnable
            }
            if (dialog == null) {
                dialog = WaitDialog.Builder(this, automaticShutdown).setCancelable(false)
            }
            dialog?.setMessage(message)
            if (!isShowDialog()) {
                dialog!!.show()
            }
        }, delayMillis)
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        if (dialogCount > 0) {
            dialogCount--
        }
        if ((dialogCount != 0) || (dialog == null) || !isShowDialog()) {
            return
        }
        dialog?.dismiss()
    }

    override fun initLayout() {
        super.initLayout()

        val titleBar = getTitleBar()
        titleBar?.setOnTitleBarListener(this)
        titleBar?.titleView?.paint?.isFakeBoldText = true

        // 初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            getStatusBarConfig().init()

            // 设置标题栏沉浸
            if (titleBar != null) {
                ImmersionBar.setTitleBar(this, titleBar)
            }
        }

        val messageId = getString(KEY_MESSAGE_ID)
        if (this !is HomeActivity) {
            reportClickMessage(messageId)
        }
    }

    open fun reportClickMessage(messageId: String?) {
        if (messageId != null) {
            EasyHttp.post(this).api(ClickMessageApi(messageId))
                .request(object : OnHttpListener<HttpData<ClickMessageApi.Bean?>> {
                    override fun onHttpSuccess(data: HttpData<ClickMessageApi.Bean?>) {}
                    override fun onHttpFail(throwable: Throwable?) {}
                })
        }
    }

    /**
     * 是否使用沉浸式状态栏
     */
    protected open fun isStatusBarEnabled(): Boolean {
        return true
    }

    /**
     * 状态栏字体深色模式
     */
    open fun isStatusBarDarkFont(): Boolean {
        return true
    }

    /**
     * 解决软键盘与底部输入框冲突问题，默认为false
     */
    protected open fun keyboardConflictEnable(): Boolean {
        return false
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    open fun getStatusBarConfig(): ImmersionBar {
        if (immersionBar == null) {
            immersionBar = createStatusBarConfig()
        }
        return immersionBar!!
    }

    /**
     * 初始化沉浸式状态栏
     */
    protected open fun createStatusBarConfig(): ImmersionBar {
        return ImmersionBar.with(this) // 默认状态栏字体颜色为黑色
            .statusBarDarkFont(isStatusBarDarkFont()) // 指定导航栏背景颜色
            .keyboardEnable(keyboardConflictEnable())// 解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
            .navigationBarColor(R.color.white) // 状态栏字体和导航栏内容自动变色，必须指定状态栏颜色和导航栏颜色才可以自动变色
            .autoDarkModeEnable(true, 0.2f)
    }

    /**
     * 设置标题栏的标题
     */
    override fun setTitle(@StringRes id: Int) {
        title = getString(id)
    }

    /**
     * 设置标题栏的标题
     */
    override fun setTitle(title: CharSequence?) {
        super<BaseActivity>.setTitle(title)
        getTitleBar()?.title = title
    }

    override fun getTitleBar(): TitleBar? {
        if (titleBar == null) {
            titleBar = obtainTitleBar(getContentView())
        }
        return titleBar
    }

    override fun onLeftClick(titleBar: TitleBar?) {
        onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isShowDialog()) {
            hideDialog()
        }
        dialog = null
    }

    override fun attachBaseContext(newBase: Context?) {
        // 绑定语种
        super.attachBaseContext(MultiLanguages.attach(newBase))
    }
}