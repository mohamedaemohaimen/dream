package com.tt.dramatime.ui.adapter.home

import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import com.smart.adapter.util.ScreenUtils
import com.tt.dramatime.app.BaseVBHolder
import com.tt.dramatime.app.BaseVBSingleItemAdapter
import com.tt.dramatime.databinding.HomeHeadLayoutBinding
import com.tt.dramatime.http.api.BannerApi.Bean
import com.tt.dramatime.ui.activity.BrowserActivity
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.ui.activity.wallet.StoreActivity
import com.tt.dramatime.util.AlphaPageTransformer
import com.tt.dramatime.util.ScaleInTransformer
import com.youth.banner.util.BannerUtils

/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 首页头部banner
 * </pre>
 */
class HomeHeadAdapter(private val owner: LifecycleOwner) :
    BaseVBSingleItemAdapter<HomeHeadLayoutBinding, List<Bean>>() {

    override fun onBindViewHolder(holder: BaseVBHolder<HomeHeadLayoutBinding>, item: List<Bean>?) {

        /**
         * 画廊效果
         */
        val height = (ScreenUtils.getScreenWidth(context) - BannerUtils.dp2px(75f)) * 1.568

        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, height.toInt()
        )
        holder.binding.banner.layoutParams = layoutParams

        holder.binding.banner.setAdapter(BannerAdapter(item))
        holder.binding.banner.setIndicator(holder.binding.indicator, false)
        holder.binding.banner.addBannerLifecycleObserver(owner)
        holder.binding.banner.isAutoLoop(true)
        holder.binding.banner.setLoopTime(5000)
        //添加画廊效果 scale设置1不用自带的缩放效果 自己设置缩放效果解决rtl问题
        holder.binding.banner.setBannerGalleryEffect(29, 0, 1f)
        //(可以和其他PageTransformer组合使用，比如AlphaPageTransformer，注意但和其他带有缩放的PageTransformer会显示冲突)
        //添加透明效果(画廊配合透明效果更棒)
        holder.binding.banner.addPageTransformer(AlphaPageTransformer())
        holder.binding.banner.addPageTransformer(ScaleInTransformer())
        holder.binding.banner.setBannerRound2(16f)
        holder.binding.banner.setOnBannerListener { _, position ->
            item?.get(position)?.apply {
                when (urlType) {
                    "1" -> seriesDetail?.let { videoModel ->
                        PlayerActivity.start(context, videoModel.movieId)
                    }

                    "2" -> StoreActivity.start(context)

                    "3" -> url?.let { BrowserActivity.start(context, it) }
                }
            }
        }
    }

}