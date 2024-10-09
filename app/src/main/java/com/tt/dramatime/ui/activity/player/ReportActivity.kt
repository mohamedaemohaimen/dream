package com.tt.dramatime.ui.activity.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.hjq.bar.TitleBar
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseViewBindingActivity
import com.tt.dramatime.databinding.ReportActivityBinding
import com.tt.dramatime.http.api.MovieReportApi
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.widget.flowlayout.FlowLayout
import com.tt.dramatime.widget.flowlayout.FlowLayout.Companion.RIGHT
import com.tt.dramatime.widget.flowlayout.TagAdapter
import com.tt.dramatime.widget.flowlayout.TagFlowLayout
import com.tt.dramatime.widget.fonttext.FontTextView

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 举报页面
 * </pre>
 */
class ReportActivity :
    BaseViewBindingActivity<ReportActivityBinding>({ ReportActivityBinding.inflate(it) }) {

    companion object {
        private const val KEY_MOVIE_ID = "key.movie.id"
        fun start(context: Context, movieVideosId: String) {
            val intent = Intent(context, ReportActivity::class.java)
            intent.putExtra(KEY_MOVIE_ID, movieVideosId)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private var movieId: String? = null
    /**默认选中第一项*/
    private var type = "1"

    override fun initView() {
        if (LanguageManager.isArabicLocale(getContext())) {
            binding.reportTag.setGravity(RIGHT)
        }

        binding.reportTag.adapter = object :
            TagAdapter<String>(getContext().resources.getStringArray(R.array.report_content)) {
            override fun getView(parent: FlowLayout?, position: Int, bean: String?): View {
                val rootView: FontTextView = layoutInflater.inflate(
                    R.layout.tag_item, binding.reportTag, false
                ) as FontTextView
                rootView.text = bean
                return rootView
            }
        }

        binding.reportTag.setOnTagClickListener(object : TagFlowLayout.OnTagClickListener {
            override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
                type = (position + 1).toString()
                return true
            }
        })
    }

    override fun initData() {
        movieId = getString(KEY_MOVIE_ID)
    }

    override fun onRightClick(titleBar: TitleBar?) {
        if (binding.contentEt.text.toString().isEmpty()) {
            toast(R.string.please_enter_reason)
            return
        }
        if (binding.emailEt.text.toString().isEmpty()) {
            toast(R.string.please_enter_email)
            return
        }
        report()
    }

    /**剧集举报*/
    private fun report() {
        movieId?.let { movieId ->
            EasyHttp.post(this).api(
                MovieReportApi(
                    movieId,
                    type,
                    binding.contentEt.text.toString(),
                    binding.emailEt.text.toString()
                )
            ).request(object : HttpCallbackProxy<HttpData<MovieReportApi.Bean>>(this) {
                override fun onHttpSuccess(result: HttpData<MovieReportApi.Bean>?) {
                    finish()
                }
            })
        }
    }

}