package com.tt.dramatime.util

import android.view.View
import com.tt.dramatime.R
import com.youth.banner.transformer.BasePageTransformer

class AlphaPageTransformer : BasePageTransformer {

    private var mMinAlpha = DEFAULT_MIN_ALPHA

    constructor()

    constructor(minAlpha: Float) {
        mMinAlpha = minAlpha
    }

    override fun transformPage(view: View, position: Float) {
        if (position < -1 || position > 1) {
            view.scaleX = 0.85f
            view.scaleY = 0.85f
        } else if (position <= 1) { // [-1,1]
            if (position < 0) {
                val scaleX = 1 + 0.1f * position
                view.scaleX = scaleX
                view.scaleY = scaleX
            } else {
                val scaleX = 1 - 0.1f * position
                view.scaleX = scaleX
                view.scaleY = scaleX
            }
        }


        val imageView = view.findViewById<View>(R.id.bannerBg)
        imageView.scaleX = 0.999f //hack
        if (position < -1) { // [-Infinity,-1)
            imageView.alpha = mMinAlpha
        } else if (position <= 1) { // [-1,1]
            //[0，-1]
            val factor: Float = if (position < 0) {
                //[1,min]
                mMinAlpha - (1 + position)
            } else { //[1，0]
                //[min,1]
                mMinAlpha - (1 - position)
            }
            imageView.alpha = factor
        } else { // (1,+Infinity]
            imageView.alpha = mMinAlpha
        }
    }

    companion object {
        private const val DEFAULT_MIN_ALPHA = 1.0f
    }
}
