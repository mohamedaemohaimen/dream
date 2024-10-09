package com.tt.dramatime.util

import android.view.View
import com.tt.dramatime.manager.LanguageManager
import com.youth.banner.transformer.BasePageTransformer


/**
 * <pre>
 *   @author : wiggins
 *   Date: 2024/2/21
 *   desc : 解决反向布局动画问题  position = -position
 * </pre>
 */
class ScaleInTransformer : BasePageTransformer {
    private var mMinScale = DEFAULT_MIN_SCALE

    constructor()
    constructor(minScale: Float) {
        mMinScale = minScale
    }

    override fun transformPage(view: View, position: Float) {
        var mPosition = position
        if (LanguageManager.isArabicLocale(view.context)) {
            mPosition = -mPosition
        }
        val pageWidth = view.width
        val pageHeight = view.height
        view.pivotY = (pageHeight / 2).toFloat()
        view.pivotX = (pageWidth / 2).toFloat()
        if (mPosition < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.scaleX = mMinScale
            view.scaleY = mMinScale
            view.pivotX = pageWidth.toFloat()
        } else if (mPosition <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            if (mPosition < 0) //1-2:1[0,-1] ;2-1:1[-1,0]
            {
                val scaleFactor = (1 + mPosition) * (1 - mMinScale) + mMinScale
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
                view.pivotX = pageWidth * (DEFAULT_CENTER + DEFAULT_CENTER * -mPosition)
            } else  //1-2:2[1,0] ;2-1:2[0,1]
            {
                val scaleFactor = (1 - mPosition) * (1 - mMinScale) + mMinScale
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
                view.pivotX = pageWidth * ((1 - mPosition) * DEFAULT_CENTER)
            }
        } else { // (1,+Infinity]
            view.pivotX = 0f
            view.scaleX = mMinScale
            view.scaleY = mMinScale
        }
    }

    companion object {
        private const val DEFAULT_MIN_SCALE = 0.85f
    }
}
