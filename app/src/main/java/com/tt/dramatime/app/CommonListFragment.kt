package com.tt.dramatime.app

import android.view.View
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tt.dramatime.R
import com.tt.dramatime.databinding.CommonListFragmentBinding
import com.tt.dramatime.ui.adapter.CustomLoadMoreAdapter
import com.tt.dramatime.widget.fonttext.FontTextView

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/12/8
 *   desc : 只用来显示的列表、直接继承此类使用
 * </pre>
 */
abstract class CommonListFragment<A : AppActivity> :
    BaseViewBindFragment<CommonListFragmentBinding, A>({ CommonListFragmentBinding.inflate(it) }),
    OnRefreshListener {

    /**
     * 适配器
     */
    private lateinit var mAdapter: BaseQuickAdapter<*, *>
    private lateinit var helper: QuickAdapterHelper

    /**
     * 当前页码
     */
    protected var currentPage = AppConstant.FIRST_PAGE

    /**
     * 子类重写该方法，返回 adapter；
     *
     * @return BaseQuickAdapter
     */
    protected abstract fun getAdapter(): BaseQuickAdapter<*, *>

    /**
     * 子类重写该方法来写 具体请求接口的逻辑
     *
     * @param page 当前请求的 页码
     */
    protected abstract fun requestData(page: Int)

    /**
     * 手动滑的下拉刷新功能是否开启 ， 默认开启
     */
    protected open fun isNeedRefresh(): Boolean {
        return true
    }

    override fun initView() {
        mAdapter = getAdapter()
        // 使用默认的"加载更多"的样式
        helper = QuickAdapterHelper.Builder(mAdapter)
            .setTrailingLoadStateAdapter(CustomLoadMoreAdapter()).build()

        helper.trailingLoadStateAdapter?.setOnLoadMoreListener(object :
            TrailingLoadStateAdapter.OnTrailingListener {
            override fun onLoad() {
                requestData(currentPage)
            }

            override fun onFailRetry() {
                requestData(currentPage)
            }

            override fun isAllowLoading(): Boolean {
                // 下拉刷新的适合，不允许进行"加载更多"
                return !binding.refreshSl.isRefreshing
            }
        })

        binding.listRv.adapter = helper.adapter
        mAdapter.isStateViewEnable = true
        mAdapter.animationEnable = false

        binding.refreshSl.setOnRefreshListener(this)

    }

    override fun initData() {
        binding.refreshSl.autoRefresh()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        refresh()
    }

    /**
     * 下拉刷新、无动画
     */
    open fun refresh() {
        currentPage = AppConstant.FIRST_PAGE
        //刷新的话滚动回首行
        binding.listRv.scrollToPosition(0)
        // 重置“加载更多”时状态
        helper.trailingLoadState = LoadState.None
        requestData(currentPage)
    }

    /**
     * 接口返回成功
     * 处理从接口返回来的数据逻辑
     */
    protected open fun <T> successData(list: List<T>) {
        if (!isAdded) {
            return
        }
        if (currentPage == AppConstant.FIRST_PAGE) {
            //取消下拉刷新转圈
            binding.refreshSl.finishRefresh()
            if (list.isEmpty()) {
                //说明没有数据，需显示没有数据占位图
                mAdapter.stateView = getEmptyDataView()
            }
            //如果是加载的第一页数据，用 submitList()
            mAdapter.submitList(list as List<Nothing>)

            //预加载条数
            helper.trailingLoadStateAdapter?.preloadSize = (list.size / 2).coerceAtLeast(5)
        } else {
            //加载更多完成了 ， 此时可以下拉刷新
            binding.refreshSl.isEnabled = isNeedRefresh()
            // Logger.d("--------------------- 加载完成 、 启用下拉刷新");
            mAdapter.addAll(list as List<Nothing>)
        }

        if (list.isEmpty()) {
            if (currentPage == AppConstant.FIRST_PAGE) {
                helper.trailingLoadState = LoadState.None
            } else {
                //置状态为未加载，并且没有分页数据了
                helper.trailingLoadState = LoadState.NotLoading(true)
            }
        } else {
            //设置状态为未加载，并且还有分页数据
            helper.trailingLoadState = LoadState.NotLoading(false)
        }

        //判断不满一屏，禁止加载更多
        helper.trailingLoadStateAdapter?.checkDisableLoadMoreIfNotFullPage()
        //记录当前页数
        currentPage += 1
    }

    /**
     * 接口返回失败
     * 处理失败逻辑
     */
    fun failure(throwable: Throwable) {
        if (currentPage == AppConstant.FIRST_PAGE) {
            //取消下拉刷新转圈
            binding.refreshSl.finishRefresh()
            //第一页，之前就没有数据需要显示网络错误占位图
            if (getAdapter().items.isEmpty()) {
                getAdapter().stateView = getErrorView()
            }
            //Logger.d("--------------------- 刷新失败 、 启用上拉加载");
        } else {
            //加载更多完成了 ， 此时可以下拉刷新
            binding.refreshSl.isEnabled = isNeedRefresh()
            helper.trailingLoadState = LoadState.Error(throwable)
            //Logger.d("--------------------- 加载失败 、 启用下拉刷新");
        }
    }

    fun getEmptyDataView(): View {
        val mEmptyDataView =
            View.inflate(context, R.layout.view_list_load_empty_data, null)
        val tvEmpty = mEmptyDataView.findViewById<FontTextView>(R.id.tv_empty)
        tvEmpty?.text = getEmptyDataTip()
        return mEmptyDataView
    }

    fun getErrorView(): View {
        val mErrorView = View.inflate(context, R.layout.view_list_load_error, null)
        mErrorView?.setOnClickListener { refresh() }
        return mErrorView!!
    }

    fun getEmptyDataTip(): String {
        return context?.getString(R.string.text_empty_no_data).toString()
    }

    override fun onDestroyView() {
        binding.refreshSl.setOnRefreshListener(null)
        super.onDestroyView()

    }

}