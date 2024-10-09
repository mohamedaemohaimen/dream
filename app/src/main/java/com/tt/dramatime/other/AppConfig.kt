package com.tt.dramatime.other

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import com.tt.dramatime.BuildConfig
import com.tt.dramatime.http.db.MMKVDurableConstant
import com.tt.dramatime.http.db.MMKVExt
import java.util.UUID


/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/3/4
 *   desc : App 配置管理类
 * </pre>
 */
object AppConfig {

    var HOST_URL = BuildConfig.HOST_URL

    /**
     * 当前是否为调试模式
     */
    fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    /**
     * 获取当前构建的模式
     */
    fun getBuildType(): String {
        return BuildConfig.BUILD_TYPE
    }

    /**
     * 当前是否要开启日志打印功能
     */
    fun isLogEnable(): Boolean {
        return BuildConfig.LOG_ENABLE
    }

    /**
     * 获取当前应用的包名
     */
    fun getPackageName(): String {
        return BuildConfig.APPLICATION_ID
    }

    /**
     * 获取当前应用的版本名ba
     */
    fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    /**
     * 获取当前应用的版本码
     */
    fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    /**
     * 获取服务器主机地址
     */
    fun getHostUrl(): String {
        return HOST_URL
    }

    /**
     * 获取UserAgent
     */
    fun getUserAgent(): String {
        val userAgent: String
        val versionInfo = if (isDebug()) "DramaTime_test/" else "DramaTime/"
        //APP版本
        val versionName = versionInfo + getVersionName()
        //model
        val model = Build.MODEL + "/" + Build.VERSION.RELEASE
        //系统版本
        val systemVersion = Build.BRAND + " " + Build.VERSION.RELEASE
        userAgent = "$versionName(Android;$systemVersion;$model)"
        val sb = StringBuilder()
        var i = 0
        val length = userAgent.length
        while (i < length) {
            val c = userAgent[i]
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", c.code))
            } else {
                sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    fun getCountry(context: Context): String? {

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simCountry = tm.simCountryIso

        if (simCountry != null && simCountry.length == 2) { // SIM country code is available
            return simCountry
        } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // Device is not 3G (would be unreliable)
            val networkCountry = tm.networkCountryIso
            return if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                networkCountry
            } else ""
        }

        return context.resources.configuration.locales[0].country
    }

    /**
     * 获取数字版权管理设备ID
     *
     * @return WidevineID，可能为空
     */
    fun getWidevineID(context: Context): String {
//        return "0000000000000000000000000000000000000000000000000000000000000001"
        var deviceId = MMKVExt.getDurableMMKV()?.decodeString(MMKVDurableConstant.KEY_DEVICE_ID)
        if (deviceId.isNullOrEmpty()) {
            try {
                //See https://stackoverflow.com/questions/16369818/how-to-get-crypto-scheme-uuid
                //You can find some UUIDs in the https://github.com/google/ExoPlayer source code
                //val COMMON_PSSH_UUID =  UUID(0x1077EFECC0B24D02L, 0xACE33C1E52E2FB4BL)
                //val CLEARKEY_UUID =  UUID(0xE2719D58A985B3C9L, 0x781AB030AF78D30EL)
                //val WIDEVINE_UUID =  UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL)
                //val PLAYREADY_UUID =  UUID(0x9A04F07998404286L, 0xAB92E65BE0885F95L)
                val widevineUUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
                val mediaDrm = MediaDrm(widevineUUID)
                val widevineId = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                val sb = java.lang.StringBuilder()
                for (aByte in widevineId) {
                    sb.append(String.format("%02x", aByte))
                }
                val defaultUUID = "0000000000000000000000000000000000000000000000000000000000000000"
                deviceId = if (sb.toString() == defaultUUID) getUUID(context) else sb.toString()
                return deviceId
            } catch (e: Exception) {
                deviceId = getUUID(context)
                Firebase.crashlytics.recordException(Exception("getWidevineID.Exception：${e.message}"))
                Logger.e("getWidevineID().Exception==$e")
            } catch (e: Error) {
                deviceId = getUUID(context)
                Firebase.crashlytics.recordException(Error("getWidevineID.Error：${e.message}"))
                Logger.e("getWidevineID().Error==$e")
            }
            MMKVExt.getDurableMMKV()?.encode(MMKVDurableConstant.KEY_DEVICE_ID, deviceId)
            return deviceId.toString()
        } else {
            return deviceId
        }
    }

    @SuppressLint("HardwareIds")
    fun getUUID(context: Context): String {
        val mAndroidId = MMKVExt.getDurableMMKV()?.decodeString(MMKVDurableConstant.KEY_ANDROID_ID)
        if (mAndroidId.isNullOrEmpty()){
            val uuId: String
            val androidId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            uuId = if (androidId.isEmpty() || "9774d56d682e549c" == androidId) UUID.randomUUID()
                .toString() else UUID(
                androidId.hashCode().toLong(), androidId.hashCode().toLong() shl 32
            ).toString()
            MMKVExt.getDurableMMKV()?.encode(MMKVDurableConstant.KEY_ANDROID_ID, uuId)
            return uuId
        }
        return mAndroidId
    }

}