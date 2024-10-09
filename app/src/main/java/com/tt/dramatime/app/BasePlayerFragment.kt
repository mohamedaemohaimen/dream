package com.tt.dramatime.app

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.tt.base.BaseDialog
import com.tt.base.action.BundleAction
import com.tt.base.action.ClickAction
import com.tt.base.action.HandlerAction
import com.tt.base.action.KeyboardAction
import com.tt.dramatime.action.ToastAction
import com.tt.dramatime.ui.dialog.WaitDialog

/**
 * @Author leo
 * @Address https://github.com/lihangleo2
 * @Date 2024/1/5
 * 因为很多人并不知道放置视频时如何使用，故放上我在项目里的Base封装的几个重要的类
 */
abstract class BasePlayerFragment : Fragment(), HandlerAction, ClickAction, BundleAction,
    KeyboardAction ,ToastAction{
    private var isLoaded = false

    /** 加载对话框 */
    private var dialog: BaseDialog? = null

    /** 对话框数量 */
    private var dialogCount: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        if (!isLoaded && !isHidden) {
            lazyInit()
            isLoaded = true
        }

        if (!isHidden) {
            onVisible()
        }
    }

    override fun onPause() {
        super.onPause()
        onInVisible()
    }

    override fun onDestroy() {
        super.onDestroy()
        isLoaded = false
        removeCallbacks()
    }

    override fun getBundle(): Bundle? {
        return arguments
    }

    /**
     * 当前加载对话框是否在显示中
     */
    open fun isShowDialog(): Boolean {
        return dialog != null && dialog!!.isShowing
    }

    /**
     * 显示加载对话框
     */
    open fun showDialog(message: String = "") {
        if (isAdded.not() ) {
            return
        }
        dialogCount++
        postDelayed(Runnable {
            if ((dialogCount <= 0) ||isAdded.not()) {
                return@Runnable
            }
            if (dialog == null) {
                dialog = context?.let {
                    WaitDialog.Builder(it)
                        .setMessage(message)
                        .setCancelable(false)
                        .create()
                }
            }
            if (!dialog!!.isShowing) {
                dialog!!.show()
            }
        }, 300)
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideDialog() {
        if (isAdded.not()) {
            return
        }
        if (dialogCount > 0) {
            dialogCount--
        }
        if ((dialogCount != 0) || (dialog == null) || !dialog!!.isShowing) {
            return
        }
        dialog?.dismiss()
    }

    /**
     * 初始化
     */
    protected open fun initView() {}

    /**
     * 懒加载
     */
    protected open fun lazyInit() {}

    /**
     * 显示
     */
    protected open fun onVisible() {}

    /**
     * 隐藏
     */
    protected open fun onInVisible() {}
}