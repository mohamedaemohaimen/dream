package com.tt.dramatime.widget.subtitleview

import android.widget.TextView
import com.tt.dramatime.util.subtitleFile.srt.Subtitle

/**
 * 字幕控制接口
 */
interface ISubtitleControl {

    /**
     * 设置数据
     *
     * @param subtitles
     */
    fun setSubtitleData(subtitles: ArrayList<Subtitle>, subtitleStyle: String?)

    /**
     * 设置字幕
     *
     * @param view
     * @param item
     */
    fun setItemSubtitle(view: TextView?, item: String?)

    /**
     * 设置是否显示字幕
     *
     * @param type
     */
    fun setDisplay(type: Int)

    /**
     * 开始
     */
    fun setStart()

    /**
     * 暂停
     */
    fun setPause()

    /**
     * 定位设置字幕，单位毫秒
     *
     * @param position
     */
    fun seekTo(position: Long)

    /**
     * 停止
     */
    fun setStop()

    /**
     * 后台播放
     *
     * @param pb
     */
    fun setPlayOnBackground(pb: Boolean)
}
