package com.tt.dramatime.util

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.facebook.applinks.AppLinkData
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.hjq.language.MultiLanguages
import com.orhanobut.logger.Logger
import com.tt.dramatime.app.AppApplication
import com.tt.dramatime.app.AppConstant
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_AD_COMPLETION_REGISTER
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_AD_ID
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_CONTENT_LANGUAGE
import com.tt.dramatime.http.db.MMKVDurableConstant.Companion.KEY_INSTALL_REFERRER
import com.tt.dramatime.http.db.MMKVExt
import com.tt.dramatime.util.ReferrerUtils.getFaceBookInstallReferrer
import org.json.JSONException
import org.json.JSONObject
import java.net.URLDecoder
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * <pre>
 *   @Author : wiggins
 *   Date:  2024/4/16 14:23
 *   Desc :  Google Play Install Referrer API库
 * </pre>
 */
object InstallReferrer {

    private var init = false
    private var googleInstallReferrerCompliance = false

    /**
     * 初始化Play Install Referrer 库
     */
    fun initReferrerClient(context: Context) {
        if (init) return
        //启动并与 Play 商店应用的连接
        val referrerClient: InstallReferrerClient =
            InstallReferrerClient.newBuilder(context).build()

        referrerClient.startConnection(object : InstallReferrerStateListener {
            //onInstall Referrer安装完成
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            //连接已建立后获取安装引荐来源
                            val response: ReferrerDetails = referrerClient.installReferrer
                            //已安装软件包的引荐来源网址。
                            val installReferrer: String = response.installReferrer

                            setGoogleInstallReferrer(installReferrer)

                            Logger.e("InstallReferrerClient.response-->\n推荐url-->$installReferrer")
                            //LP的installReferrer信息需要先解码
                            if (installReferrer.contains("dramatime") && installReferrer.contains("playvideo")) {
                                val decodeReferrer = URLDecoder.decode(installReferrer, "UTF-8")
                                Logger.e("InstallReferrer.decode-->$decodeReferrer")
                                val contentMap = ReferrerUtils.parseData(decodeReferrer)
                                setLangAdId(contentMap["lang"], contentMap["adId"], context)
                            } else {
                                parseInstallReferrer(installReferrer, context)
                            }

                            //断开服务连接(断开连接将有助于避免出现泄露和性能问题。)
                            referrerClient.endConnection()
                            init = true
                        } catch (e: Exception) {
                            Logger.e("InstallReferrerClient.error:${e.message}")
                            Firebase.crashlytics.recordException(Exception("InstallReferrerClient.error:${e.message}"))
                            e.printStackTrace()
                        }
                    }

                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        //当前Play Store应用程序上没有API。
                        Logger.e("InstallReferrerClient-->当前Play Store应用程序上没有API。")
                    }

                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        //无法建立连接。
                        Logger.e("InstallReferrerClient-->无法建立连接。")
                    }

                    else -> Logger.e("InstallReferrerClient.responseCode-->$responseCode")
                }

            }

            //onInstall Referrer服务已断开连接
            override fun onInstallReferrerServiceDisconnected() {
                Logger.e("InstallReferrerClient-->服务已断开连接")
                //尝试在下次请求时重新启动连接
                //通过调用startConnection（）方法来Google Play。
            }
        })
    }

    private val LANG_CODE_PATTERN: Pattern = Pattern.compile("LANG=([^|]+)")

    /**
     * 解析InstallReferrer
     */
    fun parseInstallReferrer(installReferrer: String?, context: Context) {

        val referrerMap = ReferrerUtils.parseQueryString(installReferrer)

        if (referrerMap["utm_source"] == "apps.facebook.com") {
            try {
                referrerMap["utm_content"]?.let { content ->
                    val sourceJson = JSONObject(content).getJSONObject("source")
                    //FaceBook installReferrer 需要解密
                    val contentObject = JSONObject(
                        AesGcm256Utils.decrypt(
                            sourceJson.getString("nonce"), sourceJson.getString("data")
                        )
                    )
                    Logger.e("contentObject:$contentObject")
                    var lang: String? = null
                    val campaignName = contentObject.optString("campaign_name")
                    if (campaignName.isNotEmpty()) {
                        val langMatcher: Matcher = LANG_CODE_PATTERN.matcher(campaignName)
                        if (langMatcher.find()) {
                            lang = langMatcher.group(1)?.lowercase()
                        }
                    }
                    setLangAdId(lang, contentObject.optString("adgroup_id"), context)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Firebase.crashlytics.recordException(JSONException("${e.message}"))
            }
        } else if (googleInstallReferrerCompliance.not()) {
            //如果InstallReferrer来源解析不是Facebook或者LP，则用Facebook获取InstallReferrer的方法再获取一遍
            googleInstallReferrerCompliance = true
            AppApplication.appContext.apply {
                val faceBookInstallReferrer = getFaceBookInstallReferrer(this)
                Logger.e("faceBookInstallReferrer:$faceBookInstallReferrer")
                if (faceBookInstallReferrer.isNotEmpty()) {
                    setGoogleInstallReferrer(faceBookInstallReferrer)
                    parseInstallReferrer(faceBookInstallReferrer, this)
                } else {
                    AppLinkData.fetchDeferredAppLinkData(this) { appLinkData ->
                        Logger.e("AppLinkData.fetchDeferredAppLinkData: ${appLinkData?.targetUri}")
                        // 处理延迟深度链接
                        appLinkData?.targetUri?.let { uri ->
                            // 例如导航到 dramatime://app/?sourceCode=c3191f
                            if (uri.host == "app") {
                                val lang = uri.getQueryParameter("LANG")?.lowercase()
                                AppConstant.sourceCode = uri.getQueryParameter("sourceCode")
                                MMKVExt.getDurableMMKV()?.apply {
                                    if (decodeString(KEY_CONTENT_LANGUAGE).isNullOrEmpty()) {
                                        lang?.let {
                                            AppConstant.adLang = it
                                            MultiLanguages.setAppLanguage(context, Locale(it))
                                            encode(KEY_CONTENT_LANGUAGE, it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setGoogleInstallReferrer(installReferrer: String?) {
        MMKVExt.getDurableMMKV()?.encode(KEY_INSTALL_REFERRER, installReferrer)
    }

    private fun setLangAdId(lang: String?, adId: String?, context: Context) {
        AppConstant.adLang = lang
        MMKVExt.getDurableMMKV()?.apply {
            if (decodeString(KEY_AD_COMPLETION_REGISTER).isNullOrEmpty()) {
                lang?.let {
                    MultiLanguages.setAppLanguage(context, Locale(it))
                    encode(KEY_CONTENT_LANGUAGE, it)
                }
                adId?.let { encode(KEY_AD_ID, it) }
            }
        }
    }
}