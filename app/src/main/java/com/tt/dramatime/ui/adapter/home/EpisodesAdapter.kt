package com.tt.dramatime.ui.adapter.home

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.ScreenUtils
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBQuickAdapter
import com.tt.dramatime.databinding.EpisodesItemBinding
import com.tt.dramatime.http.api.HomeListApi
import com.tt.dramatime.http.api.HomeListApi.Bean.MovieListBean
import com.tt.dramatime.manager.LanguageManager
import com.tt.dramatime.ui.activity.home.VideoCatalogActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.util.GlideUtils

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 剧集列表适配器
 * </pre>
 */
class EpisodesAdapter(
    dataList: MutableList<MovieListBean>,
    private val linear: Boolean,
    private val pageStatus: Boolean,
    private val moreDataList: MutableList<HomeListApi.Bean>? = null,
    private val code: String? = "",
    private val blockStyle: String? = "",
    private val hasLabel: Boolean = true
) : BaseVBQuickAdapter<EpisodesItemBinding, MovieListBean>(dataList) {

    private var width = 0
    private var lastWidth = 0

    init {
        width = (ScreenUtils.getScreenWidth() * 0.385).toInt()
        lastWidth = (width * 0.4892).toInt()
    }

    override fun onBindViewHolder(
        holder: BaseVBHolder<EpisodesItemBinding>, position: Int, item: MovieListBean?
    ) {
        holder.binding.apply {
            val params = labelIv.layoutParams as FrameLayout.LayoutParams
            params.height =
                dp2px(if (blockStyle == "3") 22.1f else if (blockStyle == "6") 14f else 17f)
            labelIv.layoutParams = params

            val layoutParams: LinearLayout.LayoutParams = if (linear) {
                titleTv.minLines = 2
                LinearLayout.LayoutParams(
                    if (position == itemCount - 1 && pageStatus) {
                        posterRl.setSizeRatio(68f, 188f)
                        titleTv.visibility = View.GONE
                        moreFl.visibility = View.VISIBLE
                        lastWidth
                    } else {
                        posterRl.setSizeRatio(139f, 188f)
                        titleTv.visibility = View.VISIBLE
                        moreFl.visibility = View.GONE
                        width
                    }, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            } else {
                titleTv.minLines = 1
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            // 设置控件的布局参数
            episodesLl.layoutParams = layoutParams
            item?.apply {
                GlideUtils.loadImage(context, poster, posterIv)
                //GlideUtils.loadSmallImage(context, poster,0.9f, posterIv)
                titleTv.text = title
                episodesLl.setOnClickListener {
                    movieId?.let { movieId ->
                        if (pageStatus && position == itemCount - 1) {
                            code?.let { code ->
                                moreDataList?.let { moreDataList ->
                                    VideoCatalogActivity.start(context, moreDataList, code)
                                }
                            }
                        } else {
                            PlayerActivity.start(context, movieId)
                        }
                    }
                }

                labelIv.scaleType =
                    if (LanguageManager.isArabicLocale(context)) ImageView.ScaleType.FIT_START else ImageView.ScaleType.FIT_END

                labelIv.visibility = if (corner != null && hasLabel) {
                    GlideUtils.loadImageTransparent(context, corner?.image, labelIv)
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

}