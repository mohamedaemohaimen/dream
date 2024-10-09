package com.tt.dramatime.ui.activity.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.tt.dramatime.action.ListAction
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.SearchActivityBinding
import com.tt.dramatime.http.api.HomeListApi
import com.tt.dramatime.http.api.SearchMovieApi
import com.tt.dramatime.http.api.SearchRecommendApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.adapter.CustomLoadMoreAdapter
import com.tt.dramatime.ui.adapter.home.EpisodesAdapter
import com.tt.dramatime.ui.adapter.home.SearchPopularAdapter
import com.tt.dramatime.ui.adapter.home.SearchRecommendAdapter
import com.tt.dramatime.ui.adapter.home.SearchResultAdapter
import com.tt.dramatime.util.HomeGridSpacingItemDecoration

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 搜索页面
 * </pre>
 */
class SearchActivity :
    BaseViewBindingActivity<SearchActivityBinding>({ SearchActivityBinding.inflate(it) }),
    TextWatcher, ListAction, OnLoadMoreListener {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SearchActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val mSearchRecommendAdapter = SearchRecommendAdapter(mutableListOf())
    private val mSearchResultAdapter = SearchResultAdapter(mutableListOf())
    private val mEpisodesAdapter = EpisodesAdapter(
        mutableListOf(), false, pageStatus = false, blockStyle = "6"
    )

    private var searchPageNum = 1
    private var likePageNum = 1

    private var query: String? = null

    private var searchNotEmpty = false

    override fun initView() {

        binding.apply {
            postDelayed({
                searchEt.requestFocus()
                showKeyboard(searchEt)
            }, 300)

            searchEt.addTextChangedListener(this@SearchActivity)
            searchEt.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = searchEt.text.toString()
                    performSearch(query) // 调用搜索逻辑
                    true
                } else {
                    false
                }
            }

            refreshSl.setOnLoadMoreListener(this@SearchActivity)

            helper.addBeforeAdapter(SearchPopularAdapter())

            val linearItemDecoration = object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                ) {
                    val childPosition = parent.getChildAdapterPosition(view)
                    outRect.bottom = ConvertUtils.dp2px( if (childPosition == 0) 13f else 16f)
                }
            }

            popularRv.addItemDecoration(linearItemDecoration)
            popularRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && keyboardVisible(searchEt)) {
                        hideKeyboard(searchEt)
                    }
                }
            })

            resultRv.addItemDecoration(linearItemDecoration)
            resultRv.adapter = mSearchResultAdapter

            mSearchResultAdapter.setOnItemClickListener { adapter, view, position ->
                val url = mSearchResultAdapter.items[position].url
                if (url != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    mSearchResultAdapter.items[position].id?.let {
                        PlayerActivity.start(getContext(), it)
                    }
                }
            }

            val layoutManager = GridLayoutManager(getContext(), 3)
            likeRv.layoutManager = layoutManager
            likeRv.addItemDecoration(
                HomeGridSpacingItemDecoration(
                    3, ConvertUtils.dp2px(16f), ConvertUtils.dp2px(11f)
                )
            )

            likeRv.adapter = mEpisodesAdapter

            setOnClickListener(searchCancelTv, deleteIv)
        }
    }

    override fun initData() {
        initListLayout()
        refresh()
    }

    private fun performSearch(query: String) {
        // 实现你的搜索逻辑
        hideKeyboard(binding.searchEt)
        // 比如：过滤列表、请求API等
        if (query.isNotEmpty()) {
            showDialog()
            binding.refreshSl.resetNoMoreData()
            searchPageNum = 1
            this.query = query
            // 搜索逻辑
            searchResult()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(view: View) {
        when (view) {
            binding.searchCancelTv -> finish()
            binding.deleteIv -> binding.searchEt.setText("")
        }
    }

    /**
     * [TextWatcher]
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (s?.isNotEmpty() == true) {
            binding.searchEt.textSize = 14f
            binding.deleteIv.visibility = View.VISIBLE
        } else {
            binding.searchEt.textSize = 12f
            binding.deleteIv.visibility = View.GONE
        }
    }

    override fun getRv(): RecyclerView {
        return binding.popularRv
    }

    override fun getAdapter(): BaseQuickAdapter<*, *> {
        return mSearchRecommendAdapter
    }

    override fun getListContext(): Context {
        return getContext()
    }

    override fun getSmartRefreshLayout(): SmartRefreshLayout? {
        return null
    }

    override var currentPage: Int = 1

    override var helper: QuickAdapterHelper = QuickAdapterHelper.Builder(mSearchRecommendAdapter)
        .setTrailingLoadStateAdapter(CustomLoadMoreAdapter()).build()

    override fun requestData(page: Int) {
        columnsList(1, page)
    }

    private fun columnsList(type: Int, page: Int = 1) {
        //You Might Like
        EasyHttp.get(this).api(SearchRecommendApi(type, if (type == 1) page else likePageNum))
            .request(object : OnHttpListener<HttpData<HomeListApi.Bean>?> {
                override fun onHttpSuccess(result: HttpData<HomeListApi.Bean>?) {
                    result?.getData()?.let {
                        if (type == 1) {
                            successData(it.movieList as List<Nothing>)
                        } else {
                            binding.refreshSl.finishLoadMore()
                            if (it.movieList.isNullOrEmpty().not()) {
                                if (likePageNum == 1) {
                                    mEpisodesAdapter.submitList(it.movieList)
                                } else {
                                    mEpisodesAdapter.addAll(it.movieList!!)
                                }
                            } else {
                                binding.refreshSl.setNoMoreData(true)
                            }
                            likePageNum++
                        }
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    throwable?.let { failure(throwable) }
                }
            })
    }

    private fun searchResult() {
        if (query == null) return
        EasyHttp.get(this).api(SearchMovieApi(query!!, searchPageNum))
            .request(object : OnHttpListener<HttpData<SearchMovieApi.Bean>?> {
                override fun onHttpSuccess(result: HttpData<SearchMovieApi.Bean>?) {
                    hideDialog()
                    result?.getData()?.let {
                        binding.refreshSl.finishLoadMore()
                        searchResultUI(it.list?.isNotEmpty() == true)
                        mSearchResultAdapter.setKeyWords(query!!)
                        it.list?.let { list ->
                            if (searchPageNum == 1) {
                                mSearchResultAdapter.submitList(list)
                            } else {
                                mSearchResultAdapter.addAll(list)
                            }
                        }
                        searchPageNum++
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    hideDialog()
                    searchResultUI(false)
                    binding.refreshSl.finishLoadMore()
                }
            })
    }

    private fun searchResultUI(isNotEmpty: Boolean) {
        searchNotEmpty = isNotEmpty

        binding.popularRv.visibility = View.GONE
        binding.refreshSl.visibility = View.VISIBLE

        if (searchPageNum == 1) {
            if (isNotEmpty) {
                binding.emptyLl.visibility = View.GONE
                binding.mayLikeTv.visibility = View.GONE
                binding.likeRv.visibility = View.GONE
            } else {
                binding.emptyLl.visibility = View.VISIBLE
                binding.mayLikeTv.visibility = View.VISIBLE
                binding.likeRv.visibility = View.VISIBLE
                likePageNum = 1
                columnsList(2)
            }
        } else {
            binding.refreshSl.setNoMoreData(isNotEmpty.not())
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        if (searchNotEmpty) {
            searchResult()
        } else {
            columnsList(2)
        }
    }

    override fun onDestroy() {
        binding.refreshSl.setOnLoadMoreListener(null)
        super.onDestroy()
    }

}