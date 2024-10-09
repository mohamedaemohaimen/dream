package com.tt.dramatime.widget.fonttext

import android.content.Context
import android.graphics.Typeface

/**
 * @author : Jary
 * E-mail :  jarylan@foxmail.com
 * Date : 2021/6/17
 * Desc : 字体
 * Version : v1.4.0
 */
class FontFactory(var context: Context) {

    companion object {
        const val DIN_BOLD = 1
        const val DIN_MEDIUM = 2
        const val DIN_REGULAR = 3
        const val SF_PRO_HEAVY = 4
    }

    val mDinBold: Typeface by lazy {
        Typeface.createFromAsset(context.assets, "font/DIN-Bold.otf")
    }


    val mDinMedium: Typeface by lazy {
        Typeface.createFromAsset(context.assets, "font/DIN-Medium.otf")
    }


    val mDinRegular: Typeface by lazy {
        Typeface.createFromAsset(context.assets, "font/DIN-Regular.otf")
    }

    val mHeavyItalic: Typeface by lazy {
        Typeface.createFromAsset(context.assets, "font/SFProDisplay-HeavyItalic.otf")
    }


}