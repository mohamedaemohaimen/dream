package com.tt.dramatime.ui.activity.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tt.dramatime.app.CommonListActivity
import com.tt.dramatime.http.api.HomeListApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.adapter.home.EpisodesMoreRankAdapter

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 更多排行榜剧集
 * </pre>
 */
class MoreRankActivity : CommonListActivity() {

    companion object {
        private const val KEY_TITLE = "key.title"
        private const val KEY_CODE = "key.code"

        fun start(context: Context, title: String?, code: String) {
            val intent = Intent(context, MoreRankActivity::class.java)
            intent.putExtra(KEY_TITLE, title)
            intent.putExtra(KEY_CODE, code)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val mEpisodesAdapter by lazy {
        EpisodesMoreRankAdapter(mutableListOf())
    }

    override fun getAdapter(): BaseQuickAdapter<*, *> {
        return mEpisodesAdapter
    }

    override fun initView() {
        super.initView()
        setTitle(getString(KEY_TITLE))

        val layoutManager = LinearLayoutManager(getContext())
        binding.listRv.layoutManager = layoutManager

        val linearItemDecoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                val childPosition = parent.getChildAdapterPosition(view)
                outRect.top = ConvertUtils.dp2px(if (childPosition == 0) 16f else 0f)
            }
        }
        binding.listRv.addItemDecoration(linearItemDecoration)
    }

    override fun requestData(page: Int) {
        EasyHttp.get(this).api(HomeListApi(getString(KEY_CODE), page))
            .request(object : OnHttpListener<HttpData<List<HomeListApi.Bean>>?> {
                override fun onHttpSuccess(result: HttpData<List<HomeListApi.Bean>>?) {
                    result?.getData()?.let {
                        successData(if (it.isNotEmpty()) it[0].movieList as List<Nothing> else mutableListOf())
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    throwable?.let { failure(throwable) }
                }
            })
    }

}