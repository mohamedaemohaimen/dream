package com.tt.dramatime.http.db

import android.text.TextUtils
import com.hjq.gson.factory.GsonFactory
import com.orhanobut.logger.Logger
import com.tt.dramatime.http.api.MovieDetailApi
import com.tt.dramatime.http.bean.VideoModel
import com.tt.dramatime.ui.activity.player.PlayerActivity.Companion.UNLOCKED

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2022/12/7
 *   desc : 用户资料管理
 * </pre>
 */
class MovieDetailHelper(val movieId: String) {

    private var _movieDetailBean: MovieDetailApi.Bean? = null
    val movieDetailBean: MovieDetailApi.Bean? get() = _movieDetailBean

    init {
        if (_movieDetailBean == null) {
            try {
                val jsonStr = MMKVExt.getUserMmkv()?.decodeString(movieId)
                Logger.d("movieDetailBean===$jsonStr")
                if (!TextUtils.isEmpty(jsonStr)) {
                    _movieDetailBean = GsonFactory.getSingletonGson().fromJson(
                        jsonStr, MovieDetailApi.Bean::class.java
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setMovieDetailBean(movieDetailBean: MovieDetailApi.Bean) {
        _movieDetailBean = movieDetailBean
        saveMovieDetailBean()
    }

    fun addList(videoModel: VideoModel) {
        movieDetailBean?.list?.add(videoModel)
        saveMovieDetailBean()
    }

    fun setList(list: MutableList<VideoModel>) {
        movieDetailBean?.list = list
        saveMovieDetailBean()
    }

    fun updateWatchHistory(currentEpisode: Int) {
        movieDetailBean?.watchHistory?.currentEpisode = currentEpisode
        saveMovieDetailBean()
    }

    fun updateUnlock(currentEpisode: Int) {
        movieDetailBean?.list?.forEachIndexed { index, videoModel ->
            if (currentEpisode == videoModel.ep) {
                movieDetailBean?.list?.get(index)?.unlockStatus = UNLOCKED
                saveMovieDetailBean()
                Logger.e("updateUnlock成功")
                return
            }
        }
    }

    private fun saveMovieDetailBean() {
        if (_movieDetailBean == null) {
            return
        }
        MMKVExt.getUserMmkv()
            ?.encode(movieId, GsonFactory.getSingletonGson().toJson(movieDetailBean))
    }

}