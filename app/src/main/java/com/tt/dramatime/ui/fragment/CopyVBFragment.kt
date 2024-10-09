package com.tt.dramatime.ui.fragment

import com.tt.dramatime.app.BaseViewBindFragment
import com.tt.dramatime.databinding.CopyFragmentBinding
import com.tt.dramatime.ui.activity.CopyVBActivity

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 可进行拷贝的副本
 * </pre>
 */
class CopyVBFragment :
    BaseViewBindFragment<CopyFragmentBinding, CopyVBActivity>({ CopyFragmentBinding.inflate(it) }) {

    companion object {

        fun newInstance(): CopyVBFragment {
            return CopyVBFragment()
        }
    }

    override fun initView() {}

    override fun initData() {}
}