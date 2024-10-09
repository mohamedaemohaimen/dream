package com.tt.dramatime.ui.adapter.home

import android.view.LayoutInflater
import android.view.View
import com.tt.dramatime.R
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.SearchResultItemBinding
import com.tt.dramatime.http.api.SearchMovieApi.Bean.ListBean
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.util.SpannableStringUtils
import com.tt.dramatime.widget.flowlayout.FlowLayout
import com.tt.dramatime.widget.flowlayout.FlowLayout.Companion.RIGHT
import com.tt.dramatime.widget.flowlayout.TagAdapter
import com.tt.dramatime.widget.fonttext.FontTextView


/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 搜索结果适配器
 * </pre>
 */
class SearchResultAdapter(dataList: MutableList<ListBean>) :
    BaseVBQuickAdapter<SearchResultItemBinding, ListBean>(dataList) {

    private var keywords: String? = null

    fun setKeyWords(keywords: String) {
        this.keywords = keywords
    }

    override fun onBindViewHolder(
        holder: BaseVBHolder<SearchResultItemBinding>, position: Int, item: ListBean?
    ) {
        item?.apply {
            holder.binding.apply {
                GlideUtils.loadImage(context, cover, posterIv)
                if (LanguageManager.isJapaneseLocale(context)) {
                    titleTv.setLineSpacing(0f, 0.8f)
                    introduceTv.setLineSpacing(0f, 0.8f)
                } else if (LanguageManager.isSpainLocale(context)) {
                    titleTv.setLineSpacing(0f, 0.9f)
                    introduceTv.setLineSpacing(0f, 0.9f)
                }

                SpannableStringUtils.highlightFirstKeyword(
                    titleTv, title, keywords, getColor(R.color.color_C640FF)
                )

                introduceTv.text = summary
                epTv.text = context.getString(R.string.ep_current, totalEpisode)

                if (LanguageManager.isArabicLocale(context)) {
                    epTag.setGravity(RIGHT)
                }

                epTag.adapter = object : TagAdapter<String>(tags) {
                    override fun getView(parent: FlowLayout?, position: Int, bean: String?): View {
                        val rootView = LayoutInflater.from(context).inflate(
                            R.layout.tag_ep_lable_item, epTag, false
                        ) as FontTextView
                        rootView.text = bean
                        return rootView
                    }
                }

                sourceGp.visibility = if (url != null && icon != null) {
                    GlideUtils.loadImageTransparent(context, icon, thirdSourcesIv)
                    View.VISIBLE
                } else View.GONE
            }
        }
    }

}