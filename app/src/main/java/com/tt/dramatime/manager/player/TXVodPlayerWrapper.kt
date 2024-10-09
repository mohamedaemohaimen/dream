package com.tt.dramatime.manager.player

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.ScreenUtils.getScreenWidth
import com.hjq.gson.factory.GsonFactory
import com.orhanobut.logger.Logger
import com.tencent.liteav.txcplayer.model.TXSubtitleRenderModel
import com.tencent.rtmp.ITXVodPlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXLiveConstants.ERR_LICENSE_CHECK_FAIL
import com.tencent.rtmp.TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION
import com.tencent.rtmp.TXPlayInfoParams
import com.tencent.rtmp.TXTrackInfo
import com.tencent.rtmp.TXVodConstants
import com.tencent.rtmp.TXVodConstants.EVT_KEY_SELECT_TRACK_ERROR_CODE
import com.tencent.rtmp.TXVodPlayConfig
import com.tencent.rtmp.TXVodPlayer
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.rtmp.ui.TXSubtitleView
import com.tt.dramatime.R
import com.tt.dramatime.app.AppApplication
import com.tt.dramatime.http.bean.SubtitleStyleBean
import com.tt.dramatime.http.bean.VideoModel
import com.tt.dramatime.manager.player.TXCSDKService.appId
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/1/4
 *   desc : 腾讯VOD功能封装
 * </pre>
 */
class TXVodPlayerWrapper(
    context: Context?,
    mVideoModel: VideoModel,
    private val mode: Int = TXVodConstants.RENDER_MODE_ADJUST_RESOLUTION
) : ITXVodPlayListener {

    private val vodPlayer: TXVodPlayer
    private val mTXVodPlayConfig: TXVodPlayConfig
    private var mOnPlayEventChangedListener: OnPlayEventChangedListener? = null
    private var mStatus: TxVodStatus? = null
    private var mVideoModel: VideoModel
    private var mStartOnPrepare = false
    private var initSubtitleStyle = false
    private var mSubtitleStyle: SubtitleStyleBean? = null
    private var selectTrack = false

    init {
        this.mVideoModel = mVideoModel
        vodPlayer = TXVodPlayer(context)
        vodPlayer.setVodListener(this)
        vodPlayer.setRenderMode(mode)

        mTXVodPlayConfig = TXVodPlayConfig().apply {
            progressInterval = 100 //默认500
            isSmoothSwitchBitrate = true
            maxBufferSize = 10f
            maxPreloadSize = 1.5f
            preferredResolution = (720 * 1280).toLong()
        }
        vodPlayer.setConfig(mTXVodPlayConfig)
    }

    fun setVideoModel(mVideoModel: VideoModel) {
        this.mVideoModel = mVideoModel
    }

    fun setPlayerView(txCloudVideoView: TXCloudVideoView) {
        vodPlayer.setPlayerView(txCloudVideoView)
    }

    fun setSubtitle(subtitleView: TXSubtitleView) {
        vodPlayer.setSubtitleView(subtitleView)
    }

    fun setSubtitleStyle(subtitleStyle: String?) {
        initSubtitleStyle = false
        subtitleStyle?.let {
            mSubtitleStyle =
                GsonFactory.getSingletonGson().fromJson(it, SubtitleStyleBean::class.java)
        }
    }

    private fun addSubtitleSource() {
        if (selectTrack.not()) {
            vodPlayer.addSubtitleSource(
                mVideoModel.srtUrl, mVideoModel.srtUrl, TXVodConstants.VOD_PLAY_MIMETYPE_TEXT_SRT
            )
        }
    }

    fun preStartPlay() {
        mStatus = TxVodStatus.TX_VIDEO_PLAYER_STATUS_UNLOAD
        mStartOnPrepare = false
        vodPlayer.isLoop = false
        vodPlayer.stopPlay(true)
        Logger.i(TAG + "[preStartPlay] , startOnPrepare ，" + mStartOnPrepare + "， mVodPlayer " + vodPlayer.hashCode() + " url " + url)
        vodPlayer.setAutoPlay(false)

        //付费视频用fileId播放
        if (mVideoModel.url.isNotEmpty()) {
            vodPlayer.startVodPlay(mVideoModel.url)
        } else {
            vodPlayer.startVodPlay(
                TXPlayInfoParams(appId, mVideoModel.fileId, mVideoModel.playSignature)
            )
        }

        addSubtitleSource()
    }

    fun resumePlay() {
        Logger.i(TAG + "[resumePlay] , startOnPrepare， " + mStartOnPrepare + " mVodPlayer " + vodPlayer.hashCode() + " url " + url)
        if (mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_STOPPED) {
            vodPlayer.isLoop = false
            vodPlayer.setAutoPlay(true)
            //付费视频用fileId播放
            if (mVideoModel.url.isNotEmpty()) {
                vodPlayer.startVodPlay(mVideoModel.url)
            } else {
                vodPlayer.startVodPlay(
                    TXPlayInfoParams(appId, mVideoModel.fileId, mVideoModel.playSignature)
                )
            }
            addSubtitleSource()
            playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING)
            return
        }
        if (mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_PREPARED || mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_PAUSED) {
            vodPlayer.resume()
            playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING)
        } else {
            mStartOnPrepare = true
        }
    }

    fun pausePlay() {
        vodPlayer.pause()
        playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PAUSED)
    }

    fun stopForPlaying() {
        if (mStatus == TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING) {
            vodPlayer.stopPlay(true)
            playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_STOPPED)
        }
    }

    fun stopPlay() {
        vodPlayer.stopPlay(true)
        selectTrack = false
        playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_STOPPED)
    }

    override fun onPlayEvent(txVodPlayer: TXVodPlayer, event: Int, bundle: Bundle) {

        if (event != TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            Logger.i("$TAG[onPlayEvent] default== $event, bundle$bundle,url==$url")
        }

        when (event) {
            TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED -> {
                playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PREPARED)
                //选择外挂字幕
                val subtitleTrackInfoList: List<TXTrackInfo> = vodPlayer.subtitleTrackInfo
                Logger.i(TAG + "[onPlayEvent] , startOnPrepare，" + mStartOnPrepare + "，mVodPlayer " + vodPlayer.hashCode() + " ep " + mVideoModel.ep + " subtitleTrackInfoList.size:${subtitleTrackInfoList.size}")
                for (track in subtitleTrackInfoList) {
                    Logger.d(TAG + "TrackIndex= " + track.getTrackIndex() + " ,name= " + track.getName())
                    vodPlayer.selectTrack(track.trackIndex)
                    selectTrack = true
                }

                if (mStartOnPrepare) {
                    vodPlayer.resume()
                    mStartOnPrepare = false
                    playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_PLAYING)
                }
            }

            TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME -> {
                mOnPlayEventChangedListener?.onRcvFirstFrame()
            }

            TXLiveConstants.PLAY_EVT_PLAY_PROGRESS -> {
                mOnPlayEventChangedListener?.onProgress(bundle)
            }

            TXLiveConstants.PLAY_EVT_PLAY_LOADING -> {
                mOnPlayEventChangedListener?.onLoading(true)
            }

            TXLiveConstants.PLAY_EVT_VOD_LOADING_END -> {
                mOnPlayEventChangedListener?.onLoading(false)
            }

            TXLiveConstants.PLAY_EVT_PLAY_BEGIN -> {
                mOnPlayEventChangedListener?.onLoading(false)
            }

            TXLiveConstants.PLAY_ERR_GET_PLAYINFO_FAIL -> {
                mOnPlayEventChangedListener?.onGetPlayInfoFail()
                playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_STOPPED)
            }

            TXLiveConstants.PLAY_EVT_PLAY_END -> {
                mOnPlayEventChangedListener?.onLoadEnd()
                playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_ENDED)
            }

            TXVodConstants.VOD_PLAY_EVT_SELECT_TRACK_COMPLETE -> {
                if (bundle.getInt(EVT_KEY_SELECT_TRACK_ERROR_CODE) != 0) {
                    //EVT_KEY_SELECT_TRACK_ERROR_CODE.error:11022116 字幕错误返回11022116
                    mOnPlayEventChangedListener?.onLoadFail(event, "event:$event,message:$bundle")
                }
                Logger.e(
                    "EVT_KEY_SELECT_TRACK_ERROR_CODE.error:" + bundle.getInt(
                        EVT_KEY_SELECT_TRACK_ERROR_CODE
                    )
                )
            }

            TXVodConstants.VOD_PLAY_ERR_DECODE_SUBTITLE_FAIL or TXLiveConstants.PLAY_ERR_NET_DISCONNECT or TXLiveConstants.PLAY_ERR_HLS_KEY or TXVodConstants.VOD_PLAY_ERR_SYSTEM_PLAY_FAIL or TXVodConstants.VOD_PLAY_ERR_DECODE_VIDEO_FAIL or TXVodConstants.VOD_PLAY_ERR_DECODE_AUDIO_FAIL or TXVodConstants.VOD_PLAY_ERR_DECODE_SUBTITLE_FAIL or TXVodConstants.VOD_PLAY_ERR_RENDER_FAIL or TXVodConstants.VOD_PLAY_ERR_PROCESS_VIDEO_FAIL or TXVodConstants.VOD_PLAY_ERR_DECODE_SUBTITLE_FAIL or ERR_LICENSE_CHECK_FAIL -> {
                mOnPlayEventChangedListener?.onLoadFail(event, "event:$event,message:$bundle")
                playerStatusChanged(TxVodStatus.TX_VIDEO_PLAYER_STATUS_STOPPED)
            }
        }
    }

    override fun onNetStatus(txVodPlayer: TXVodPlayer, bundle: Bundle) {
        // 获取实时速率, 单位：kbps
        val speed = bundle.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)
        mOnPlayEventChangedListener?.onSpeed(speed)
        //获取视频宽高度
        val videoWidth = bundle.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)
        val videoHeight = bundle.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)

        if (initSubtitleStyle.not() && videoWidth > 0 && videoHeight > 0) {

            val canvasWidth = getScreenWidth() - dp2px(if (isAdjust()) 40f else 98 + 20f)

            //98为拦截局两边的间隙 20为字幕画布的padding
            val interceptionHeight: Int =
                getScreenWidth().minus(dp2px(98f)).times(1.34).minus(dp2px(20f)).toInt()

            val canvasHeight =
                if (isAdjust()) getScreenWidth() * videoHeight / videoWidth else interceptionHeight

            Logger.d("SubtitleStyle.canvasWidth：$canvasWidth canvasHeight：$canvasHeight")

            val model = TXSubtitleRenderModel()
            // 字幕渲染画布的宽
            model.canvasWidth = canvasWidth
            // 字幕渲染画布的高
            model.canvasHeight = canvasHeight
            // 设置字幕字体是否为粗体
            model.isBondFontStyle = false
            // 设置字幕样式
            if (mSubtitleStyle != null) {
                mSubtitleStyle?.apply {
                    val fontSize = dp2px(fontSize ?: 24f).toFloat()
                    model.fontColor = getColor(textColor)
                    strokeColo?.let {
                        model.outlineColor = getColor(it)
                    }
                    model.fontSize = if (isAdjust()) fontSize else fontSize * 0.76f
                    model.verticalMargin = srtPositionRate ?: 0.20.toFloat()
                    //Logger.e("srtPositionRate:$srtPositionRate")
                }
            } else {
                model.fontColor = ContextCompat.getColor(AppApplication.appContext, R.color.white)
                model.fontSize = dp2px(if (isAdjust()) 24f else 24f * 0.76f).toFloat()
                model.verticalMargin = 0.2f
            }

            vodPlayer.setSubtitleStyle(model)
            initSubtitleStyle = true
        }
    }

    private fun isAdjust(): Boolean {
        return mode == RENDER_MODE_ADJUST_RESOLUTION
    }

    private fun getColor(color: String?): Int {
        return Color.parseColor("#${color ?: "FFFFFF"}")
    }

    /**
     * 创建内部文件路径
     */
    fun createInternalFilePath(context: Context, fileName: String): String {
        // 获取应用内部存储路径
        val file = File(context.filesDir, fileName)

        // 如果文件不存在，则创建
        if (!file.exists()) {
            file.createNewFile()
        }

        return file.absolutePath
    }

    /**
     * Base64转文件
     */
    fun base64ToFile(
        base64String: String,
        filePath: String,
        onComplete: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

        try {
            val file = File(filePath)
            FileOutputStream(file).use { fileOutputStream ->
                fileOutputStream.write(decodedBytes)
                fileOutputStream.close()
                onComplete(file)
            }
        } catch (e: IOException) {
            onError(e)
            e.printStackTrace()
        }
    }

    fun seekTo(time: Float) {
        vodPlayer.seek(time, true)
        if (!vodPlayer.isPlaying) {
            vodPlayer.resume()
            mOnPlayEventChangedListener?.onPlaying()
        }
    }

    val isPlaying: Boolean
        get() = vodPlayer.isPlaying

    private fun playerStatusChanged(status: TxVodStatus) {
        mStatus = status
        Logger.i(TAG + "[playerStatusChanged] mVodPlayer " + vodPlayer.hashCode() + " mStatus " + mStatus)
    }

    fun setVodChangeListener(listener: OnPlayEventChangedListener?) {
        mOnPlayEventChangedListener = listener
    }

    enum class TxVodStatus {
        TX_VIDEO_PLAYER_STATUS_UNLOAD,

        // Not loaded
        TX_VIDEO_PLAYER_STATUS_PREPARED,

        // Ready to play
        //TX_VIDEO_PLAYER_STATUS_LOADING,

        // Playing
        TX_VIDEO_PLAYER_STATUS_PLAYING,

        // Paused
        TX_VIDEO_PLAYER_STATUS_PAUSED,

        // End Playback completed
        TX_VIDEO_PLAYER_STATUS_ENDED,

        // Manually stopped playback
        TX_VIDEO_PLAYER_STATUS_STOPPED
    }

    interface OnPlayEventChangedListener {
        fun onProgress(bundle: Bundle?)
        fun onSpeed(speed: Int?)
        fun onRcvFirstFrame()
        fun onPlaying()
        fun onLoading(isLoading: Boolean)
        fun onLoadFail(event: Int, failInfo: String)
        fun onGetPlayInfoFail()
        fun onLoadEnd()
    }

    val url: String
        get() = mVideoModel.url.ifEmpty {
            "ep==${mVideoModel.ep} file_id==${mVideoModel.fileId} playSignature==${mVideoModel.playSignature}"
        } + " srt:${mVideoModel.srtUrl}"

    companion object {
        private const val TAG = "DramaTime:TXVodPlayerWrapper"
    }
}
