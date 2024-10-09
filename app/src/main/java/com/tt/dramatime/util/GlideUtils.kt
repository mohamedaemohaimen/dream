package com.tt.dramatime.util

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.tt.dramatime.R

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2021/11/27
 *   desc : Glide工具类
 * </pre>
 */
class GlideUtils {

    companion object {

        /**
         * 图片加载
         */
        fun loadImage(context: Context, path: Any?, iv: ImageView) {
            Glide.with(context).load(path).into(iv)
        }

        /**
         * 图片加载 加载中和加载失败为透明背景
         */
        fun loadImageTransparent(context: Context, path: Any?, iv: ImageView) {
            Glide.with(context).load(path).apply(RequestOptions()
                // 设置默认加载中占位图
                .placeholder(R.color.transparent)
                // 设置默认加载出错占位图
                .error(R.color.transparent)).into(iv)
        }

        /**
         * 图片加载 加载中和加载失败为黑色背景
         */
        fun loadImageBlack(context: Context, path: Any?, iv: ImageView) {
            Glide.with(context).load(path).apply(RequestOptions()
                // 设置默认加载中占位图
                .placeholder(R.color.black)
                // 设置默认加载出错占位图
                .error(R.color.black)).into(iv)
        }

        /**
         * 小图片加载
         */
        fun loadSmallImage(context: Context, path: Any?, sizeMultiplier: Float, iv: ImageView) {
            Glide.with(context).load(path).sizeMultiplier(sizeMultiplier).into(iv)
        }

        /**
         * 圆角图片加载
         */
        fun loadRoundImage(context: Context, path: Any?, roundingRadius: Int, iv: ImageView) {
            Glide.with(context).load(path)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(roundingRadius))).into(iv)
        }

    }

}