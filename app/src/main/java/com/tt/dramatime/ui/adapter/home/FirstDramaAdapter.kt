package com.tt.dramatime.ui.adapter.home

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.hjq.language.MultiLanguages
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.FirstDramaItemBinding
import com.tt.dramatime.http.api.HomeListApi.Bean.MovieListBean
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.widget.flowlayout.FlowLayout
import com.tt.dramatime.widget.flowlayout.FlowLayout.Companion.RIGHT
import com.tt.dramatime.widget.flowlayout.TagAdapter
import com.tt.dramatime.widget.fonttext.FontTextView
import java.text.SimpleDateFormat
import java.util.Date

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 首发剧栏目适配器
 * </pre>
 */
class FirstDramaAdapter(dataList: MutableList<MovieListBean>) :
    BaseVBQuickAdapter<FirstDramaItemBinding, MovieListBean>(dataList) {

    override fun onBindViewHolder(
        holder: BaseVBHolder<FirstDramaItemBinding>, position: Int, item: MovieListBean?
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

                if (activity != null) {
                    activity?.apply {
                        // 定义你想要的日期格式：mm dd ss:mm
                        val dateFormat = SimpleDateFormat("MM-dd mm:ss", MultiLanguages.getAppLanguage(context))
                        val timestamp = timestamp ?: 0L
                        // 将时间戳转换为 Date 对象
                        val date = Date(timestamp)
                        timeTv.text = dateFormat.format(date)
                        timeTv.setTextColor(getColor(R.color.color_C640FF))

                        buttonTv.text =
                            context.getString(if (remindStatus == 0) R.string.remind_me else R.string.reserved)

                        buttonIv.setBackgroundResource(if (remindStatus == 0) R.drawable.ep_reminder_ic else R.drawable.ep_reserved_ic)
                    }
                } else {
                    timeTv.text = context.getString(R.string.ep_current, totalEpisode)
                    timeTv.setTextColor(getColor(R.color.color_505050))
                    buttonIv.setBackgroundResource(R.drawable.first_drama_play_ic)
                    buttonTv.text = context.getString(R.string.play)
                }

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