package com.tt.dramatime.ui.fragment.player

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.orhanobut.logger.Logger
import com.smart.adapter.interf.SmartFragmentImpl
import com.smart.adapter.util.ScreenUtils
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXVodConstants
import com.tt.dramatime.app.BasePlayerFragment
import com.tt.dramatime.databinding.InterceptionPlayerFragmentBinding
import com.tt.dramatime.http.api.ClientReportLogApi
import com.tt.dramatime.http.bean.VideoModel
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_CONTENT_LANGUAGE
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.http.db.UserProfileHelper
import com.tt.dramatime.http.model.HttpData
import com.tt.dramatime.manager.player.TXVodPlayerWrapper
import com.tt.dramatime.ui.activity.player.PlayerActivity
import com.tt.dramatime.util.GlideUtils
import com.tt.dramatime.util.eventbus.InterceptionPlayEndNotify
import com.tt.dramatime.util.subtitleFile.srt.SRTParser
import com.tt.dramatime.util.subtitleFile.srt.Subtitle
import com.youth.banner.util.BannerUtils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/21
 *   desc : 播放器Fragment
 * </pre>
 */
class InterceptionPlayerFragment : BasePlayerFragment(), SmartFragmentImpl<VideoModel>,
    TXVodPlayerWrapper.OnPlayEventChangedListener {

    companion object {
        const val TAG = "PlayerFragment"
        const val INTENT_KEY_PARAMETERS: String = "parameters"
    }

    private lateinit var binding: InterceptionPlayerFragmentBinding
    private lateinit var mVideoModel: VideoModel

    private var isStop = false
    private var isLoadEpInfo = false
    private var getPlayInfoFailCount: Int = 0

    private lateinit var mPlayerActivity: PlayerActivity

    /**
     * 根据资源 id 获取一个 View 对象
     */
    override fun <V : View?> findViewById(@IdRes id: Int): V? {
        return binding.root.findViewById(id)
    }

    override fun initSmartFragmentData(bean: VideoModel) {
        this.mVideoModel = bean
    }

    private val mTXVodPlayerWrapper by lazy(LazyThreadSafetyMode.NONE) {
        TXVodPlayerWrapper(context, mVideoModel, TXVodConstants.RENDER_MODE_FULL_FILL_SCREEN)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = InterceptionPlayerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INTENT_KEY_PARAMETERS, mVideoModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            // 恢复数据 如果需要恢复进度 要额外保存进度数据
            val videoModel: VideoModel? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    savedInstanceState.getParcelable(INTENT_KEY_PARAMETERS, VideoModel::class.java)
                } else {
                    savedInstanceState.getParcelable(INTENT_KEY_PARAMETERS)
                }
            videoModel?.let {
                mVideoModel = videoModel
            }
        }
    }

    /**复用时只会走一次，被回收时回到该界面会重新走*/
    override fun initView() {
        mPlayerActivity = (activity as PlayerActivity)

        LogUtils.dTag(TAG, "视频初始化及预加载.ep:${mVideoModel.ep} ")

        context?.let {
            val height = (ScreenUtils.getScreenWidth(it) - BannerUtils.dp2px(98f)) * 1.34

            val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height.toInt()
            )
            binding.videoCdv.layoutParams = layoutParams

            GlideUtils.loadImageBlack(it, mVideoModel.cover, binding.coverIv)
        }
        binding.titleTv.text = mVideoModel.title

        setOnClickListener(binding.tcvVideoView)
    }

    override fun lazyInit() {
        super.lazyInit()
        //懒加载位置
        LogUtils.dTag(TAG, "页面懒加载.mVideoModel.ep==${mVideoModel.ep}")
    }

    override fun onVisible() {
        super.onVisible()
        LogUtils.dTag(TAG, "页面可见时播放视频位置.mVideoModel.ep==${mVideoModel.ep}")
        setPlayerView()
        preStartPlay()
        startPlay()
    }

    override fun onInVisible() {
        super.onInVisible()
        //如果想每次回到视频都从第一帧开始，也可以在这里处理
        stopPlayer()
        binding.coverIv.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPlayer()
        binding.tcvVideoView.onDestroy()
        isLoadEpInfo = false
        isStop = false
        LogUtils.dTag("PlayerFragment", "页面销毁.mVideoModel.ep==" + mVideoModel.ep)
    }

    @SingleClick(1000)
    override fun onClick(view: View) {
        when (view) {
            binding.tcvVideoView -> mPlayerActivity.playCurrentMovie()
        }
    }

    private fun setPlayerView() {
        mTXVodPlayerWrapper.setPlayerView(binding.tcvVideoView)
        mTXVodPlayerWrapper.setSubtitle(binding.txSubtitleView)
        mTXVodPlayerWrapper.setSubtitleStyle(mVideoModel.subtitleStyle)
        mVideoModel.apply {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    if (subtitleBase64 == null) {
                        binding.txSubtitleView.visibility = View.VISIBLE
                        return@launch
                    } else binding.txSubtitleView.visibility = View.GONE

                    //生成一个内部文件路径
                    val path = mTXVodPlayerWrapper.createInternalFilePath(
                        requireContext(), "${movieCode}-${ep}.srt"
                    )

                    var subtitles: Deferred<ArrayList<Subtitle>?>? = null

                    //将base64解码写入文件
                    mTXVodPlayerWrapper.base64ToFile(subtitleBase64!!, path, onComplete = {
                        subtitles = async {
                            SRTParser.parseSRT(it)
                        }
                    }, onError = {
                        recordException(it, "onWriteError.Exception")
                    })

                    withContext(Dispatchers.Main) {
                        val mSubtitles = subtitles?.await()
                        if (mSubtitles?.isNotEmpty() == true) {
                            binding.subtitleView.setVideoMode(TXVodConstants.RENDER_MODE_FULL_FILL_SCREEN)
                            binding.subtitleView.setSubtitleData(
                                mSubtitles, mPlayerActivity.mMovieDetailBean?.subtitleStyle
                            )
                            binding.subtitleView.visibility = View.VISIBLE
                            binding.txSubtitleView.visibility = View.GONE
                        } else {
                            binding.txSubtitleView.visibility = View.VISIBLE
                            binding.subtitleView.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    binding.txSubtitleView.visibility = View.VISIBLE
                    binding.subtitleView.visibility = View.GONE
                    recordException(e, "字幕解析.Exception")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun VideoModel.recordException(e: Exception, exceptionTitle: String) {
        Firebase.crashlytics.recordException(
            Exception(
                "$exceptionTitle:${e.message}," + "用户ID：${UserProfileHelper.getUserId()},movieCode:$movieCode,ep:$ep,lang:${
                    MMKVExt.getDurableMMKV()?.getString(KEY_CONTENT_LANGUAGE, "")
                }"
            )
        )
    }

    private fun preStartPlay() {
        mTXVodPlayerWrapper.preStartPlay()
        mTXVodPlayerWrapper.setVodChangeListener(this)
        isStop = false
        Logger.i("[preStartPlay] mVideoModel.ep==" + mVideoModel.ep + "  mVideoModel.url " + mVideoModel.url)
    }

    private fun startPlay() {
        //如果停止了播放器要重新预播放
        if (isStop) {
            setPlayerView()
            mTXVodPlayerWrapper.setVodChangeListener(this)
        }
        postDelayed({
            binding.coverIv.visibility = View.GONE
            mTXVodPlayerWrapper.resumePlay()
        }, 500)

        Logger.i("[startPlay] mTXVodPlayerWrapper.url " + mTXVodPlayerWrapper.url)
    }

    private fun stopPlayer() {
        mTXVodPlayerWrapper.stopPlay()
        mTXVodPlayerWrapper.setVodChangeListener(null)
        Logger.i("[stopPlayer] mTXVodPlayerWrapper.url " + mTXVodPlayerWrapper.url)
        isStop = true
    }

    override fun onProgress(bundle: Bundle?) {
        bundle?.let {
            val progressMS = bundle.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS)
            binding.subtitleView.seekTo(progressMS.toLong())
        }
    }

    override fun onSpeed(speed: Int?) {}

    override fun onRcvFirstFrame() {
        Logger.i("[onPrepared in TXVideoBaseView] title:${mVideoModel.title}")
    }

    override fun onPlaying() {}

    override fun onLoading(isLoading: Boolean) {}

    override fun onLoadFail(event: Int, failInfo: String) {
        EasyHttp.post(this).api(ClientReportLogApi(failInfo, "video_play"))
            .request(object : OnHttpListener<HttpData<Void>> {
                override fun onHttpSuccess(result: HttpData<Void>?) {}
                override fun onHttpFail(throwable: Throwable?) {}
            })
    }

    override fun onGetPlayInfoFail() {
        Logger.e("onGetPlayInfoFail.getPlayInfoFailCount:$getPlayInfoFailCount")
    }

    override fun onLoadEnd() {
        EventBus.getDefault().post(InterceptionPlayEndNotify())
    }

}