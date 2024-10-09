package com.tt.dramatime.app

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.hjq.http.listener.OnHttpListener
import com.tt.base.BaseActivity
import com.tt.base.BaseFragment
import com.tt.base.action.BundleAction
import com.tt.base.action.ClickAction
import com.tt.base.action.HandlerAction
import com.tt.base.action.KeyboardAction
import com.tt.dramatime.R
import com.tt.dramatime.action.ToastAction
import com.tt.dramatime.http.model.HttpData
import okhttp3.Call

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/11/29
 *   desc :项目中的 DialogFragment 基类
 * </pre>
 */
abstract class AppDialogFragment : DialogFragment(), ToastAction,
    HandlerAction, ClickAction, BundleAction, KeyboardAction, OnHttpListener<Any> {
    /**
     * 监听
     */
    private var mOnDismissListener: DialogInterface.OnDismissListener? = null

    /**
     * Activity 对象
     */
    private var attachActivity: AppActivity? = null

    /**
     * 获取布局 ID
     */
    protected abstract val layoutId: Int

    /**
     * 获取Style ID ： 默认屏幕底部风格
     */
    protected open fun getStyleId(): Int {
        return R.style.ActionSheetCenterDialogStyle
    }

    /**
     * 设置弹窗位置 : 默认屏幕中间
     */
    protected open fun getGravity(): Int {
        return Gravity.CENTER
    }

    protected open fun getDismissKeyboard(): Boolean {
        return true
    }

    /** 背景遮盖层开关 */
    protected open fun getBackgroundDimEnabled(): Boolean {
        return true
    }

    /**
     * 设置内容宽； 可设置固定值 ， 或者以下值
     * WindowManager.LayoutParams.MATCH_PARENT
     * WindowManager.LayoutParams.WRAP_CONTENT
     *
     *
     * 默认铺满
     */
    protected open fun getWidth(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    /**
     * 设置内容高； 可设置固定值 ， 或者以下值
     * WindowManager.LayoutParams.MATCH_PARENT
     * WindowManager.LayoutParams.WRAP_CONTENT
     *
     *
     * 默认自适应
     */
    protected open fun getHeight(): Int {
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    /**
     * 是否可以通过物理按键，或者屏幕区域外点击来 取消
     */
    protected open fun cancelable(): Boolean {
        return true
    }

    /**
     * 初始化控件
     */
    protected abstract fun initView()

    /**
     * 初始化数据
     */
    protected abstract fun initData()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppActivity) {
            attachActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, getStyleId())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = initRootView(inflater, container)
        if (dialog == null || dialog!!.window == null) {
            dismissAllowingStateLoss()
            return super.onCreateView(inflater, container, savedInstanceState)
        }
        val window = dialog!!.window
        window!!.setBackgroundDrawableResource(R.color.transparent)
        window.decorView.setPadding(0, 0, 0, 0)
        if (getBackgroundDimEnabled()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        val wlp = window.attributes
        wlp.gravity = getGravity()
        /* xml 的根布局自适应及铺满属性完全不被遵从;  只能从通过以下两行才能设置准确的宽高属性*/
        wlp.width = getWidth()
        wlp.height = getHeight()
        window.attributes = wlp
        isCancelable = cancelable()

        return rootView
    }

    class DismissDialog(context: Context, theme: Int) : Dialog(context, theme) {
        override fun dismiss() {
            onDismissListener?.invoke()
            super.dismiss()
        }

        var onDismissListener: (() -> Unit)? = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = context?.let { DismissDialog(it, getStyleId()) }
        dialog?.onDismissListener = {
            if (getDismissKeyboard()) {
                hideSoftInput()
            }
        }
        return dialog!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    protected open fun initRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        return inflater.inflate(layoutId, container, false)
    }

    fun setOnDismissListener(mOnDismissListener: DialogInterface.OnDismissListener?) {
        this.mOnDismissListener = mOnDismissListener
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (mOnDismissListener != null) {
            mOnDismissListener!!.onDismiss(dialog)
        }
        super.onDismiss(dialog)
    }

    private fun hideSoftInput() {
        val imm =
            attachActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        // 这里吧原来的commit()方法换成了commitAllowingStateLoss()
        ft.commitAllowingStateLoss()
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    /**
     * 当前加载对话框是否在显示中
     */
    open fun isShowDialog(): Boolean {
        return attachActivity?.isShowDialog() == true
    }

    /**
     * 显示加载对话框
     */
    open fun showDialog() {
        attachActivity?.showDialog()
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideDialog() {
        attachActivity?.hideDialog()
    }

    /**
     * [OnHttpListener]
     */
    override fun onHttpStart(call: Call) {
        showDialog()
    }

    override fun onHttpSuccess(result: Any) {
        if (result !is HttpData<*>) {
            return
        }
        toastSuccess(result.getMessage())
    }

    override fun onHttpFail(e: Throwable) {
        toastError(e.message)
    }

    override fun onHttpEnd(call: Call) {
        hideDialog()
    }

    override fun getBundle(): Bundle? {
        return arguments
    }

    /**
     * 跳转 Activity 简化版
     */
    open fun startActivity(clazz: Class<out Activity>) {
        startActivity(Intent(context, clazz))
    }

    /**
     * startActivityForResult 方法优化
     */
    open fun startActivityForResult(
        clazz: Class<out Activity>,
        callback: BaseActivity.OnActivityCallback?
    ) {
        attachActivity?.startActivityForResult(clazz, callback)
    }

    open fun startActivityForResult(intent: Intent, callback: BaseActivity.OnActivityCallback?) {
        attachActivity?.startActivityForResult(intent, null, callback)
    }

    open fun startActivityForResult(
        intent: Intent,
        options: Bundle?,
        callback: BaseActivity.OnActivityCallback?
    ) {
        attachActivity?.startActivityForResult(intent, options, callback)
    }

    /**
     * 销毁当前 Fragment 所在的 Activity
     */
    open fun finish() {
        this.activity?.let {
            if (it.isFinishing || it.isDestroyed) {
                return
            }
            it.finish()
        }
    }

    /**
     * Fragment 按键事件派发
     */
    open fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val fragments: MutableList<Fragment?> = childFragmentManager.fragments
        for (fragment: Fragment? in fragments) {
            // 这个子 Fragment 必须是 BaseFragment 的子类，并且处于可见状态
            if (fragment !is BaseFragment<*> || fragment.lifecycle.currentState != Lifecycle.State.RESUMED) {
                continue
            }
            // 将按键事件派发给子 Fragment 进行处理
            if (fragment.dispatchKeyEvent(event)) {
                // 如果子 Fragment 拦截了这个事件，那么就不交给父 Fragment 处理
                return true
            }
        }
        return when (event?.action) {
            KeyEvent.ACTION_DOWN -> onKeyDown(event.keyCode, event)
            KeyEvent.ACTION_UP -> onKeyUp(event.keyCode, event)
            else -> false
        }
    }

    /**
     * 按键按下事件回调
     */
    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 默认不拦截按键事件
        return false
    }

    /**
     * 按键抬起事件回调
     */
    open fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // 默认不拦截按键事件
        return false
    }

    override fun getContext(): Context? {
        return activity
    }
}