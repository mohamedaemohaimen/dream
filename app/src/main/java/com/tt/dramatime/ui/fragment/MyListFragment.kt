package com.tt.dramatime.ui.fragment

import android.view.View
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindTitleBarFragment
import com.tt.dramatime.databinding.MyListFragmentBinding
import com.tt.dramatime.http.api.MovieListApi
import com.tt.dramatime.http.db.MyListHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.activity.HomeActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.adapter.WatchListAdapter
import com.tt.dramatime.util.eventbus.WatchingNotify
import com.tt.dramatime.widget.fonttext.FontTextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 观看历史 Fragment
 * </pre>
 */
class MyListFragment : BaseViewBindTitleBarFragment<MyListFragmentBinding, HomeActivity>({
    MyListFragmentBinding.inflate(it)
}), OnRefreshListener {

    companion object {
        fun newInstance(): MyListFragment {
            return MyListFragment()
        }
    }

    private val mWatchListAdapter by lazy {
        WatchListAdapter(mutableListOf())
    }

    private var isLoaded = false

    override fun initView() {
        mWatchListAdapter.isStateViewEnable = true
        binding.myListRv.adapter = mWatchListAdapter
        binding.refreshSl.setOnRefreshListener(this)
        mWatchListAdapter.setOnItemClickListener { adapter, view, position ->
            val data = mWatchListAdapter.items[position]
            data.movieId?.let { movieId -> PlayerActivity.start(context, movieId) }
        }
        binding.refreshSl.autoRefresh()
    }

    override fun initData() {
        if (EventBus.getDefault().isRegistered(this).not()){
            EventBus.getDefault().register(this)
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        getMovieList()
    }

    fun getMovieList() {
        EasyHttp.get(this).api(MovieListApi())
            .request(object : OnHttpListener<HttpData<MovieListApi.Bean>> {
                override fun onHttpSuccess(result: HttpData<MovieListApi.Bean>?) {
                    binding.refreshSl.finishRefresh()
                    if (result?.getData()?.list.isNullOrEmpty()) {
                        mWatchListAdapter.stateView = getEmptyDataView(false)
                    }
                    result?.getData()?.list?.let {
                        MyListHelper.setMyList(it)
                    }
                    mWatchListAdapter.submitList(result?.getData()?.list)
                    isLoaded = true
                    hideDialog()
                }

                override fun onHttpFail(throwable: Throwable?) {
                    binding.refreshSl.finishRefresh()
                    setEmptyDataView()
                    isLoaded = true
                    hideDialog()
                }
            })
    }

    fun setEmptyDataView() {
        mWatchListAdapter.submitList(mutableListOf())
        mWatchListAdapter.stateView = getEmptyDataView(true)
    }

    fun getEmptyDataView(error: Boolean): View? {
        var mEmptyDataView: View? = null
        context?.let {
            mEmptyDataView = View.inflate(context, R.layout.view_list_load_empty_data, null)
            val tvEmpty = mEmptyDataView?.findViewById<FontTextView>(R.id.tv_empty)
            val operationBtn = mEmptyDataView?.findViewById<FontTextView>(R.id.operation_btn)
            tvEmpty?.text = getString(R.string.no_viewing_hint)
            operationBtn?.visibility = View.VISIBLE
            operationBtn?.text = getString(if (error) R.string.retry else R.string.watch_more)
            operationBtn?.setOnClickListener {
                if (error) {
                    showDialog()
                    getMovieList()
                } else getAttachActivity()?.switchFragment(0)
            }
        }

        return mEmptyDataView
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(notify: WatchingNotify) {
        Logger.e("WatchingNotify==${notify.listBean.watchEpisode}")

        if (!isLoaded) return

        var removeIndex: Int? = null

        mWatchListAdapter.items.forEachIndexed { index, listBean ->
            if (listBean.movieId == notify.listBean.movieId) {
                removeIndex = index
                return@forEachIndexed
            }
        }

        removeIndex?.let { mWatchListAdapter.removeAt(it) }
        mWatchListAdapter.add(0, notify.listBean)

        MyListHelper.setMyList(mWatchListAdapter.items)
    }

    override fun isStatusBarEnabled(): Boolean {
        // 使用沉浸式状态栏
        return !super.isStatusBarEnabled()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}