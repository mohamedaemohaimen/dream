package com.tt.dramatime.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
abstract class BaseViewBindTitleBarFragment<VB : ViewBinding, A : AppActivity>(
    val block: (LayoutInflater) -> VB
) : TitleBarFragment<A>(), OnHttpListener<Any> {

    private var _binding: VB? = null

    protected val binding: VB
        get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        loading = false
        _binding = block(layoutInflater)
        initView()
        return _binding?.root
    }

    override fun getView(): View? {
        return _binding?.root
    }

    override fun <V : View?> findViewById(id: Int): V? {
        return _binding?.root?.findViewById(id)
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