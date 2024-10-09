package com.tt.dramatime.action

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tt.dramatime.R
import com.tt.dramatime.app.AppConstant
import com.tt.dramatime.widget.fonttext.FontTextView

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/12/16
 *   desc : 列表意图
 * </pre>
 */
interface ListAction : OnRefreshListener {

    fun getRv(): RecyclerView
    fun getAdapter(): BaseQuickAdapter<*, *>
    fun getListContext(): Context
    fun getLayoutManager(): RecyclerView.LayoutManager? {
        return null
    }

    fun getSmartRefreshLayout(): SmartRefreshLayout?

    /**
     * 子类重写该方法来写 具体请求接口的逻辑
     *
     * @param page 当前请求的 页码
     */
    fun requestData(page: Int)

    /**
     * 手动滑的下拉刷新功能是否开启 ， 默认开启
     */
    fun isNeedRefresh(): Boolean {
        return true
    }

    /**
     * 上拉加载功能是否开启 ， 默认开启
     */
    fun isNeedLoadMore(): Boolean {
        return true
    }

    /**
     * 当前页码
     */
    var currentPage: Int

    var helper: QuickAdapterHelper

    /**初始化布局控件的一些参数*/
    fun initListLayout() {
        getSmartRefreshLayout()?.isEnabled = isNeedRefresh()
        getSmartRefreshLayout()?.setOnRefreshListener(this)

        if (getLayoutManager() == null) {
            getRv().layoutManager = LinearLayoutManager(getListContext())
        } else {
            getRv().layoutManager = getLayoutManager()
        }

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
                val isRefreshing = getSmartRefreshLayout()?.isRefreshing ?: false
                return isNeedLoadMore() && isRefreshing.not()
            }
        })

        getAdapter().isStateViewEnable = true
        getRv().adapter = helper.adapter
        getAdapter().animationEnable = false
    }

    /**
     * 下拉刷新、无动画
     */
    fun refresh() {
        currentPage = AppConstant.FIRST_PAGE
        helper.trailingLoadState = LoadState.None
        //刷新的话滚动回首行
        getRv().scrollToPosition(0)
        requestData(currentPage)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        refresh()
    }

    /**
     * 接口返回成功
     * 处理从接口返回来的数据逻辑
     */
    fun <T> successData(list: List<T>) {
        //取消下拉刷新转圈
        getSmartRefreshLayout()?.finishRefresh()

        if (currentPage == AppConstant.FIRST_PAGE) {
            if (list.isEmpty()) {
                //说明没有数据，需显示没有数据占位图
                getAdapter().stateView = getEmptyDataView()
            }
            //如果是加载的第一页数据，用 submitList()
            getAdapter().submitList(list as List<Nothing>)

            //预加载条数
            helper.trailingLoadStateAdapter?.preloadSize = (list.size / 2).coerceAtLeast(4)
        } else {
            //加载更多完成了 ， 此时可以下拉刷新
            getSmartRefreshLayout()?.isEnabled = isNeedRefresh()
            // Logger.d("--------------------- 加载完成 、 启用下拉刷新");
            getAdapter().addAll(list as List<Nothing>)
        }

        //判断不满一屏，禁止加载更多
        helper.trailingLoadStateAdapter?.checkDisableLoadMoreIfNotFullPage()

        if (list.isEmpty()) {
            //置状态为未加载，并且没有分页数据了
            helper.trailingLoadState =
                if (currentPage == AppConstant.FIRST_PAGE) LoadState.None else LoadState.NotLoading(
                    true
                )
        } else {
            //设置状态为未加载，并且还有分页数据
            helper.trailingLoadState = LoadState.NotLoading(false)
        }

        //记录当前页数
        currentPage++
    }

    /**
     * 接口返回失败
     * 处理失败逻辑
     */
    fun failure(throwable: Throwable) {
        if (currentPage == AppConstant.FIRST_PAGE) {
            //取消下拉刷新转圈
            getSmartRefreshLayout()?.finishRefresh()
            //第一页，之前就没有数据需要显示网络错误占位图
            if (getAdapter().items.isEmpty()) {
                getAdapter().stateView = getEmptyDataView()
            }
            //Logger.d("--------------------- 刷新失败 、 启用上拉加载");
        } else {
            //加载更多完成了 ， 此时可以下拉刷新
            getSmartRefreshLayout()?.isEnabled = isNeedRefresh()
            helper.trailingLoadState = LoadState.Error(throwable)
            //Logger.d("--------------------- 加载失败 、 启用下拉刷新");
        }
    }

    fun getEmptyDataView(): View {
        val mEmptyDataView =
            View.inflate(getListContext(), R.layout.view_list_load_empty_data, null)
        val tvEmpty = mEmptyDataView.findViewById<FontTextView>(R.id.tv_empty)
        tvEmpty?.text = getEmptyDataTip()
        return mEmptyDataView
    }

    fun getErrorView(): View {
        val mErrorView = View.inflate(getListContext(), R.layout.view_list_load_error, null)
        mErrorView?.setOnClickListener { refresh() }
        return mErrorView!!
    }

    fun getEmptyDataTip(): String {
        return getListContext().getString(R.string.text_empty_no_data)
    }
}