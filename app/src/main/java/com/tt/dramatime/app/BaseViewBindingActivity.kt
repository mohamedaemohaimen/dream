package com.tt.dramatime.app

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.hjq.http.listener.OnHttpListener
import okhttp3.Call

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/11/29
 *   desc :ViewBinding 基类
 * </pre>
 */
abstract class BaseViewBindingActivity<VB : ViewBinding>(val block: (LayoutInflater) -> VB) :
    AppActivity(), OnHttpListener<Any> {

    private var _binding: VB? = null

    protected val binding: VB
        get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    override fun initActivity() {
        _binding = block(layoutInflater)
        setContentView(binding.root)
        initSoftKeyboard()
        super.initActivity()
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun onHttpStart(call: Call?) {
        showDialog()
    }

    override fun onHttpEnd(call: Call?) {
        hideDialog()
    }

    override fun onHttpSuccess(result: Any?) {}

    override fun onHttpFail(throwable: Throwable?) {
        toast(throwable?.message)
    }

    /*override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }*/

}