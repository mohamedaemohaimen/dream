package com.tt.dramatime.ui.adapter.home

import android.view.LayoutInflater
import android.view.View
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.EpisodesRankItemBinding
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
 *   desc : 剧集排行榜列表适配器
 * </pre>
 */
class EpisodesRankAdapter(dataList: MutableList<MovieListBean>) :
    BaseVBQuickAdapter<EpisodesRankItemBinding, MovieListBean>(dataList) {


    override fun onBindViewHolder(
        holder: BaseVBHolder<EpisodesRankItemBinding>, position: Int, item: MovieListBean?
    ) {
        item?.apply {
            holder.binding.apply {
                rankIv.setBackgroundResource(
                    when (position) {
                        0 -> R.drawable.rank_first_ic
                        1 -> R.drawable.rank_second_ic
                        2 -> R.drawable.rank_third_ic
                        else -> R.drawable.rank_other_ic
                    }
                )

                rankTv.text = position.plus(1).toString()

                GlideUtils.loadImage(context, poster, posterIv)

                if (LanguageManager.isJapaneseLocale(context)) {
                    titleTv.setLineSpacing(0f, 0.8f)
                } else if (LanguageManager.isSpainLocale(context)) {
                    titleTv.setLineSpacing(0f, 0.9f)
                }

                titleTv.text = title
                episodesLl.setOnClickListener {
                    movieId?.let { movieId -> PlayerActivity.start(context, movieId) }
                }

                if (LanguageManager.isArabicLocale(context)) {
                    epTag.setGravity(RIGHT)
                }

                tagList?.let {
                    tagList= if (it.size > 1) it.subList(0, 1) else tagList
                    epTag.adapter = object : TagAdapter<String>(tagList) {
                        override fun getView(
                            parent: FlowLayout?, position: Int, bean: String?
                        ): View {
                            val rootView= LayoutInflater.from(context).inflate(
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

}