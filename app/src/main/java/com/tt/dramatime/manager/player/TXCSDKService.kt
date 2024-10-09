package com.tt.dramatime.manager.player

import android.content.Context
import com.tencent.rtmp.TXLiveBase
import com.tencent.rtmp.TXLiveBaseListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXPlayerGlobalSetting

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/1/4
 *   desc : 腾讯VOD初始化服务
 * </pre>
 */
object TXCSDKService {

    /* Please refer to the official website guide for how to obtain the
       license: https://cloud.tencent.com/document/product/454/34750
       如何获取License? 请参考官网指引 https://cloud.tencent.com/document/product/454/34750
    */

    private const val licenceUrl =
        "https://license.vod-pro.com/license/v2/1324469475_1/v_cube.license"
    private const val licenseKey = "93da776dcdeadd77f71e56d577b222ed"

    const val appId = 1324469475

    var isInit = false

    /**
     * Initialize Tencent Cloud related SDKs.
     * During the SDK initialization process, sensitive information such as the mobile phone model may be read,
     * which needs to be obtained after the user agrees to the privacy policy.
     *
     *
     * 初始化腾讯云相关sdk。
     * SDK 初始化过程中可能会读取手机型号等敏感信息，需要在用户同意隐私政策后，才能获取。
     *
     * @param appContext The application context.
     */
    fun init(appContext: Context?) {
        TXLiveBase.getInstance().setLicence(appContext, licenceUrl, licenseKey)
        // 若您服务全球用户， 配置 SDK 接入环境为全球接入环境
        TXLiveBase.setGlobalEnv("GDPR")
        TXLiveBase.setLogLevel(TXLiveConstants.LOG_LEVEL_VERBOSE)
        TXLiveBase.setListener(object : TXLiveBaseListener() {
            override fun onUpdateNetworkTime(errCode: Int, errMsg: String) {
                if (errCode != 0) {
                    TXLiveBase.updateNetworkTime()
                    isInit = true
                }
            }
        })
        TXLiveBase.updateNetworkTime()

        if (TXPlayerGlobalSetting.getCacheFolderPath() == null || TXPlayerGlobalSetting.getCacheFolderPath() == "") {
            val sdcardDir = appContext?.getExternalFilesDir(null)
            if (sdcardDir != null) {
                TXPlayerGlobalSetting.setCacheFolderPath(sdcardDir.path + "/txcache")
                //设置播放引擎的全局缓存目录和缓存大小，//单位 MB
                TXPlayerGlobalSetting.setMaxCacheSize(500)
            }
        }
    }
}
