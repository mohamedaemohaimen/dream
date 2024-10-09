package com.tt.dramatime.app

import com.tt.base.BaseFragment
import com.tt.dramatime.action.ToastAction

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 * time   : 2018/10/18
 * desc   : Fragment 业务基类
 */
abstract class AppFragment<A : AppActivity> : BaseFragment<A>(),
    ToastAction{

    /**
     * 当前加载对话框是否在显示中
     */
    open fun isShowDialog(): Boolean {
        val activity: A = getAttachActivity() ?: return false
        return activity.isShowDialog()
    }

    /**
     * 显示加载对话框
     */
    open fun showDialog() {
        getAttachActivity()?.showDialog()
    }

    /**
     * 显示加载对话框
     */
    open fun showDialog(message: String) {
        getAttachActivity()?.showDialog(message)
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideDialog() {
        getAttachActivity()?.hideDialog()
    }

}