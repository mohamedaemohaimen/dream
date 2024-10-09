package com.tt.dramatime.http.bean

import android.os.Parcelable
import com.smart.adapter.interf.SmartFragmentTypeExEntity
import com.tt.dramatime.http.api.WalletBalanceApi
import kotlinx.parcelize.Parcelize

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/1
 *   desc : 单集剧详情
 * </pre>
 */
@Parcelize
data class VideoModel(
    var ep: Int,
    var movieId: String,
    var movieCode: String,
    var seriesId: String,
    var url: String,
    var cover: String,
    var srtUrl: String,
    var fileId: String,
    var playSignature: String,
    var unlockStatus: Int,//0未解锁，1已解锁 2预解锁成功 3余额不足
    var isFirst: Boolean,//是否是第一集
    var wallet: WalletBalanceApi.Bean?,
    var subtitleStyle: String, //字幕样式，只有拦截局的时候才会在这个类里返回
    var title: String, //标题，只有拦截局的时候才会在这个类里返回
    var subtitleBase64: String? //标题，只有拦截局的时候才会在这个类里返回
) : SmartFragmentTypeExEntity(), Parcelable {
    override fun getFragmentType(): Int {
        return 0
    }

}
