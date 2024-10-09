package com.tt.dramatime.ui.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.layoutmanager.QuickGridLayoutManager
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tt.dramatime.app.AppActivity
import com.tt.dramatime.app.AppConstant
import com.tt.dramatime.app.CommonListFragment
import com.tt.dramatime.http.api.HomeListApi
import com.tt.dramatime.http.api.HomeListApi.Bean.MovieListBean
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.adapter.home.EpisodesAdapter
import com.tt.dramatime.util.HomeGridSpacingItemDecoration

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 更多剧集
 * </pre>
 */
class VideoCatalogFragment : CommonListFragment<AppActivity>() {

    companion object {
        private const val KEY_CODE = "key.code"
        private const val KEY_PAGE_NUM = "key.page.num"
        private const val KEY_DATA = "key.data"

        fun newInstance(
            code: String, movieList: ArrayList<MovieListBean>, pageNum: Int = 1
        ): VideoCatalogFragment {
            val fragment = VideoCatalogFragment()
            val args = Bundle()
            args.putString(KEY_CODE, code)
            args.putInt(KEY_PAGE_NUM, pageNum)
            args.putParcelableArrayList(KEY_DATA, movieList)
            fragment.arguments = args
            return fragment
        }
    }

    private val mEpisodesAdapter by lazy {
        EpisodesAdapter(mutableListOf(), false, pageStatus = false, hasLabel = false)
    }

    private var pageNum = 1

    override fun getAdapter(): BaseQuickAdapter<*, *> {
        return mEpisodesAdapter
    }

    override fun initView() {
        super.initView()

        val layoutManager = QuickGridLayoutManager(requireContext(), 2)
        binding.listRv.layoutManager = layoutManager
        binding.listRv.addItemDecoration(
            HomeGridSpacingItemDecoration(
                2, ConvertUtils.dp2px(16f), ConvertUtils.dp2px(16f)
            )
        )

        val linearItemDecoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                val childPosition = parent.getChildAdapterPosition(view)
                outRect.top = ConvertUtils.dp2px(if (childPosition < 2) 16f else 0f)
            }
        }
        binding.listRv.addItemDecoration(linearItemDecoration)
    }

    override fun initData() {
        val data = arguments?.getParcelableArrayList<MovieListBean>(KEY_DATA)
        if (data != null) {
            successData(data)
            pageNum = getInt(KEY_PAGE_NUM) + 1
        } else super.initData()
    }

    override fun requestData(page: Int) {
        if (page == AppConstant.FIRST_PAGE) pageNum = page
        EasyHttp.get(this).api(HomeListApi(getString(KEY_CODE), pageNum))
            .request(object : OnHttpListener<HttpData<List<HomeListApi.Bean>>?> {
                override fun onHttpSuccess(result: HttpData<List<HomeListApi.Bean>>?) {
                    result?.getData()?.let {
                        successData(if (it.isNotEmpty()) it[0].movieList as List<Nothing> else mutableListOf())
                        pageNum++
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    throwable?.let { failure(throwable) }
                }
            })
    }

}