package com.tt.dramatime.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/11/29
 *   desc :ViewBinding 基类
 * </pre>
 */
abstract class BaseViewBindDialogFragment<VB : ViewBinding>(val block: (LayoutInflater) -> VB) : AppDialogFragment() {

    private var _binding: VB? = null

    protected val binding: VB
        get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    override val layoutId: Int get() = 0

    override fun initRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        _binding = block(layoutInflater)
        return binding.root
    }

    override fun <V : View?> findViewById(id: Int): V? {
        return binding.root.findViewById(id)
    }
}