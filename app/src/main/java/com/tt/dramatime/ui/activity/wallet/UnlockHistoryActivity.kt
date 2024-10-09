package com.tt.dramatime.ui.activity.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.chad.library.adapter4.BaseQuickAdapter
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tt.dramatime.R
import com.tt.dramatime.app.CommonListActivity
import com.tt.dramatime.http.api.UnlockHistoryApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.adapter.wallet.UnlockHistoryAdapter

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 解锁历史
 * </pre>
 */
class UnlockHistoryActivity : CommonListActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, UnlockHistoryActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val mUnlockHistoryAdapter by lazy {
        UnlockHistoryAdapter(mutableListOf())
    }

    override fun getAdapter(): BaseQuickAdapter<*, *> {
        return mUnlockHistoryAdapter
    }

    override fun initView() {
        super.initView()
        setTitle(R.string.unlock_history)
        mUnlockHistoryAdapter.setOnItemClickListener { adapter, view, position ->
            mUnlockHistoryAdapter.getItem(position)?.apply {
                movieId?.let {
                    PlayerActivity.start(getContext(), it, currentEpisode)
                }
            }
        }
    }

    override fun requestData(page: Int) {
        EasyHttp.get(this).api(UnlockHistoryApi(page))
            .request(object : OnHttpListener<HttpData<UnlockHistoryApi.Bean?>> {
                override fun onHttpSuccess(data: HttpData<UnlockHistoryApi.Bean?>?) {
                    data?.getData()?.list?.let {
                        successData(it)
                    }
                }

                override fun onHttpFail(throwable: Throwable?) {
                    throwable?.let { failure(throwable) }
                }
            })
    }

    override fun getEmptyDataTip(): String {
        return getString(R.string.unlocked_hint)
    }

}