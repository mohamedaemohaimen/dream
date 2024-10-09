package com.tt.dramatime.util

import android.view.View
import androidx.viewpager2.widget.ViewPager2

/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/5/30 16:04
 *   Desc : 拦截局滑动缩放动画
 * </pre>
 */
class TransScaleFormer: ViewPager2.PageTransformer {
    companion object {
        private const val MIN_SCALE = 0.89f
    }

    override fun transformPage(page: View, position: Float) {
        if (position < -1 || position > 1) {
            page.scaleX = MIN_SCALE
            page.scaleY = MIN_SCALE
        } else if (position <= 1) { // [-1,1]
            if (position < 0) {
                val scaleX = 1 + 0.11f * position;
                page.scaleX = scaleX
                page.scaleY = scaleX
            } else {
                val scaleX = 1 - 0.11f * position;
                page.scaleX = scaleX
                page.scaleY = scaleX
            }
        }
    }
}