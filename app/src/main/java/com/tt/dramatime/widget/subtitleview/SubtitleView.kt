package com.tt.dramatime.widget.subtitleview

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.ScreenUtils
import com.hjq.gson.factory.GsonFactory
import com.orhanobut.logger.Logger
import com.tencent.rtmp.TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION
import com.tencent.rtmp.TXVodConstants
import com.tt.dramatime.R
import com.tt.dramatime.http.bean.SubtitleStyleBean
import com.tt.dramatime.util.subtitleFile.srt.Subtitle
import com.tt.widget.layout.RatioFrameLayout

/**
 * 显示字幕的图层
 */
class SubtitleView : LinearLayout, ISubtitleControl, SubtitleClickListener {

    companion object {
        /**显示字幕*/
        const val TYPE_DISPLAY: Int = 0

        /**不显示字幕*/
        const val TYPE_NONE: Int = TYPE_DISPLAY + 1

        /**更新UI*/
        private const val UPDATE_SUBTITLE = TYPE_NONE + 1
    }

    private val TAG: String = javaClass.simpleName

    /**字幕外层布局*/
    private var subLayout: RatioFrameLayout? = null

    /**字幕*/
    private var subTv: SubtitleTextView? = null

    /**字幕距离底部的间隙*/
    private var subSpace: View? = null

    /**当前显示节点*/
    private var subTitleView: View? = null

    /**字幕数据*/
    private var subtitles: ArrayList<Subtitle>? = null

    /**后台播放*/
    private var playOnBackground = false

    private var context: Context? = null

    private var mSubtitleStyle: SubtitleStyleBean? = null

    private var videoMode: Int = TXVodConstants.RENDER_MODE_ADJUST_RESOLUTION

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(context)
    }

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        this.context = context
        subTitleView = inflate(context, R.layout.subtitleview, null)
        subTv = subTitleView?.findViewById(R.id.subTitleTv)
        subSpace = subTitleView?.findViewById(R.id.sub_space)
        subLayout = subTitleView?.findViewById(R.id.sub_layout)
        subTv?.setSubtitleOnTouchListener(this)
        this.orientation = VERTICAL
        this.addView(subTitleView)
    }

    private fun isAdjust(): Boolean {
        return videoMode == RENDER_MODE_ADJUST_RESOLUTION
    }

    fun setVideoMode(mode: Int) {
        this.videoMode = mode
    }

    private fun getColor(color: String?): Int {
        return Color.parseColor("#${color ?: "FFFFFF"}")
    }


    override fun setSubtitleData(subtitles: ArrayList<Subtitle>, subtitleStyle: String?) {
        if (subtitles.isEmpty()) {
            Logger.e(TAG + "subtitle data is empty")
            return
        }

        this.subtitles = subtitles

        if (isAdjust()) {
            subLayout?.setSizeRatio(9f, 16f)
        } else {
            subLayout?.setSizeRatio(277f, 371f)
        }

        var mSrtPositionRate = 0.20f
        var mFontSize = 24f
        var mStrokeColor = "000000"
        var mTextColor = "FFFFFF"

        subtitleStyle?.let {
            mSubtitleStyle =
                GsonFactory.getSingletonGson().fromJson(it, SubtitleStyleBean::class.java)
            mSubtitleStyle?.apply {
                mSrtPositionRate = srtPositionRate ?: 0.20f
                mFontSize = fontSize ?: 24f
                mStrokeColor = strokeColo ?: "000000"
                mTextColor = textColor ?: "FFFFFF"
            }
        }

        val videoHeight = if (isAdjust()) ScreenUtils.getScreenHeight() else dp2px(371f)
        val subSpaceParams = subSpace?.layoutParams as ConstraintLayout.LayoutParams
        subSpaceParams.bottomMargin = (videoHeight * mSrtPositionRate).toInt()

        subLayout?.setPadding(
            dp2px(if (isAdjust()) 20f else 10f), 0, dp2px(if (isAdjust()) 20f else 10f), 0
        )

        subTv?.setTextColor(getColor(mTextColor))
        subTv?.setStrokeColor(
            getColor(mStrokeColor), if (isAdjust()) mFontSize else mFontSize * 0.75f
        )
        subTv?.textSize = if (isAdjust()) mFontSize else mFontSize * 0.75f
    }

    override fun setItemSubtitle(view: TextView?, item: String?) {
        view?.text = Html.fromHtml(item, Html.FROM_HTML_MODE_LEGACY)
    }

    override fun setDisplay(type: Int) {
        if (type == TYPE_DISPLAY) {
            subTv!!.visibility = VISIBLE
        } else {
            subTv!!.visibility = GONE
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        //Logger.d(TAG + "onWindowFocusChanged:" + hasWindowFocus)
    }

    override fun setStart() {}

    override fun setPause() {}

    override fun setStop() {}

    override fun seekTo(position: Long) {
        if (playOnBackground) {
            return
        }

        if (subtitles?.isNotEmpty() == true) {
            val captions = searchSub(subtitles!!, position)

            if (captions.isEmpty()){
                setItemSubtitle(subTv!!, "")
            }else{
                captions.forEach {
                    setItemSubtitle(subTv!!, it.text)
                }
            }
        }
    }

    override fun setPlayOnBackground(pb: Boolean) {
        this.playOnBackground = pb
    }

    /**
     * 采用二分法去查找当前应该播放的字幕
     *
     * @param list 全部字幕
     * @param key  播放的时间点
     */
    private fun searchSub(list: ArrayList<Subtitle>, key: Long): List<Subtitle> {
        val captions: MutableList<Subtitle> = ArrayList()
        var hasMore = false

        list.forEach {
            Logger.e("searchSub:${it}")
            if (key >= it.timeIn && key <= it.timeOut) {
                hasMore = true
                captions.add(it)
            }else if (hasMore) {
                return@forEach
            }
        }
        return captions
    }

    override fun onClickDown() {}

    override fun onClickUp() {}

}
