package com.tt.dramatime.ui.dialog

import android.view.Gravity
import com.tt.dramatime.app.BaseViewBindDialogFragment
import com.tt.dramatime.databinding.CopyDialogBinding

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/12/6
 *   desc :可进行拷贝的副本
 * </pre>
 */
class CopyDialogFragment :
    BaseViewBindDialogFragment<CopyDialogBinding>({ CopyDialogBinding.inflate(it) }) {

    companion object {
        const val TAG = "CopyDialogFragment"
    }

    override fun getGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun initView() {
    }

    override fun initData() {
    }
}