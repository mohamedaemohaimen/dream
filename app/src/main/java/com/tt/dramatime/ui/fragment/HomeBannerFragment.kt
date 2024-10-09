package com.tt.dramatime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smart.adapter.interf.SmartFragmentImpl
import com.tt.dramatime.app.BasePlayerFragment
import com.tt.dramatime.databinding.HomeBannerFragmentBinding
import com.tt.dramatime.http.bean.VideoModel

/**
 * @Author leo
 * @Date 2023/9/1
 */
class HomeBannerFragment : BasePlayerFragment(), SmartFragmentImpl<VideoModel> {
    private lateinit var mBinding: HomeBannerFragmentBinding
    private lateinit var mSourceBean: VideoModel

    override fun initSmartFragmentData(bean: VideoModel) {
        this.mSourceBean = bean
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = HomeBannerFragmentBinding.inflate(inflater, container, false);
        return mBinding.root
    }

    override fun <V : View?> findViewById(id: Int): V {
        return mBinding.root.findViewById(id)
    }

    override fun initView() {
        super.initView()
    }

    override fun lazyInit() {
        super.lazyInit()
        //懒加载位置
    }


    override fun onVisible() {
        super.onVisible()
    }

    override fun onInVisible() {
        super.onInVisible()
    }

}