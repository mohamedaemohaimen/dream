package com.tt.dramatime.ui.adapter.home

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.SearchResultItemBinding
import com.tt.dramatime.http.api.HomeListApi.Bean.MovieListBean
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.widget.flowlayout.FlowLayout
import com.tt.dramatime.widget.flowlayout.FlowLayout.Companion.RIGHT
import com.tt.dramatime.widget.flowlayout.TagAdapter
import com.tt.dramatime.widget.fonttext.FontTextView

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 搜索推荐列表适配器
 * </pre>
 */
class SearchRecommendAdapter(dataList: MutableList<MovieListBean>) :
    BaseVBQuickAdapter<SearchResultItemBinding, MovieListBean>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<SearchResultItemBinding>, position: Int, item: MovieListBean?
    ) {
        item?.apply {
            holder.binding.apply {
                GlideUtils.loadImage(context, poster, posterIv)
                if (LanguageManager.isJapaneseLocale(context)) {
                    titleTv.setLineSpacing(0f, 0.8f)
                    introduceTv.setLineSpacing(0f, 0.8f)
                } else if (LanguageManager.isSpainLocale(context)) {
                    titleTv.setLineSpacing(0f, 0.9f)
                    introduceTv.setLineSpacing(0f, 0.9f)
                }

                labelIv.scaleType =
                    if (LanguageManager.isArabicLocale(context)) ImageView.ScaleType.FIT_START else ImageView.ScaleType.FIT_END

                labelIv.visibility = if (corner != null) {
                    GlideUtils.loadImageTransparent(context, corner?.image, labelIv)
                    View.VISIBLE
                } else {
                    View.GONE
                }

                titleTv.text = title
                introduceTv.text = summary
                epTv.text = context.getString(R.string.ep_current, totalEpisode)
                episodesLl.setOnClickListener {
                    movieId?.let { movieId -> PlayerActivity.start(context, movieId) }
                }

                if (LanguageManager.isArabicLocale(context)) {
                    epTag.setGravity(RIGHT)
                }

                epTag.adapter = object : TagAdapter<String>(tagList) {
                    override fun getView(parent: FlowLayout?, position: Int, bean: String?): View {
                        val rootView = LayoutInflater.from(context).inflate(
                            R.layout.tag_ep_lable_item, epTag, false
                        ) as FontTextView
                        rootView.text = bean
                        return rootView
                    }
                }
            }

        }
    }

}