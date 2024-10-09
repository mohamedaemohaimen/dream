package com.tt.dramatime.http.model

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import com.google.gson.JsonSyntaxException
import com.hjq.gson.factory.GsonFactory
import com.hjq.http.EasyLog
import com.hjq.http.config.IRequestHandler
import com.hjq.http.exception.*
import com.hjq.http.request.HttpRequest
import com.tt.dramatime.R
import com.tt.dramatime.http.api.HttpUrls.Companion.SPARE_HOST_URL
import com.tt.dramatime.http.excepetion.ResultException
import com.tt.dramatime.http.excepetion.TokenException
import com.tt.dramatime.http.excepetion.TokenInvalidException
import com.tt.dramatime.http.model.HttpCacheManager.generateCacheKey
import com.tt.dramatime.http.model.HttpCacheManager.isCacheInvalidate
import com.tt.dramatime.http.model.HttpCacheManager.readHttpCache
import com.tt.dramatime.http.model.HttpCacheManager.setHttpCacheTime
import com.tt.dramatime.http.model.HttpCacheManager.writeHttpCache
import com.tt.dramatime.other.AppConfig
import com.tt.dramatime.util.eventbus.LoginFailedNotify
import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 *  author : Android 轮子哥
 *  github : https://github.com/getActivity/AndroidProject-Kotlin
 *  time   : 2019/12/07
 *  desc   : 请求处理类
 */
class RequestHandler constructor(private val mApplication: Application) : IRequestHandler {

    @Throws(Throwable::class)
    override fun requestSuccess(httpRequest: HttpRequest<*>, response: Response, type: Type): Any {
        if (Response::class.java == type) {
            return response
        }
        if (!response.isSuccessful) {
            replaceHostUrl(response.code)
            throw ResponseException(
                mApplication.getString(R.string.http_response_error) + "，responseCode：" + response.code + "，message：" + response.message,
                response
            )
        }
        if (Headers::class.java == type) {
            return response.headers
        }
        val body = response.body
            ?: throw NullBodyException(mApplication.getString(R.string.http_response_null_body))
        if (ResponseBody::class.java == type) {
            return body
        }

        // 如果是用数组接收，判断一下是不是用 byte[] 类型进行接收的
        if (type is GenericArrayType) {
            val genericComponentType = type.genericComponentType
            if (Byte::class.javaPrimitiveType == genericComponentType) {
                return body.bytes()
            }
        }
        if (InputStream::class.java == type) {
            return body.byteStream()
        }
        if (Bitmap::class.java == type) {
            return BitmapFactory.decodeStream(body.byteStream())
        }
        val text: String = try {
            body.string()
        } catch (e: IOException) {
            // 返回结果读取异常
            throw DataException(mApplication.getString(R.string.http_data_explain_error), e)
        }

        // 打印这个 Json 或者文本
        EasyLog.printJson(httpRequest, text)
        if (String::class.java == type) {
            return text
        }

        val result: Any = try {
            GsonFactory.getSingletonGson().fromJson<Any>(text, type)
        } catch (e: JsonSyntaxException) {
            // 返回结果读取异常
            throw DataException(mApplication.getString(R.string.http_data_explain_error), e)
        }

        if (result is HttpData<*>) {
            replaceHostUrl(result.getCode())
            val headers = response.headers
            val headersSize = headers.size
            val headersMap: MutableMap<String, String> = HashMap(headersSize)
            for (i in 0 until headersSize) {
                headersMap[headers.name(i)] = headers.value(i)
            }
            result.setHeaders(headersMap)
            if (result.isRequestSucceed()) {
                // 代表执行成功
                return result
            }
            if (result.isTokenFailure()) {
                // 代表登录失效，需要重新登录
                throw TokenException(mApplication.getString(R.string.http_token_error))
            }

            if (result.isTokenInvalid()) {
                // 代表登录失效，需要重新登录
                throw TokenInvalidException(mApplication.getString(R.string.http_token_error))
            }

            // 代表执行失败
            throw ResultException(result.getMessage(), result)
        }
        return result
    }

    /**服务器异常替换域名*/
    private fun replaceHostUrl(code: Int?) {
        //返回了5xx的HTTP状态码，通常表示服务器内部错误，这时可以认为域名访问失败然后切换域名
        if (code in 501..599 && AppConfig.isDebug().not()) {
            AppConfig.HOST_URL = SPARE_HOST_URL
        }
    }

    override fun requestFail(httpRequest: HttpRequest<*>, throwable: Throwable): Throwable {
        if (throwable is HttpException) {
            if (throwable is TokenException) {
                // 登录信息失效，跳转到登录页
                EventBus.getDefault().post(LoginFailedNotify(true))
            }
            if (throwable is TokenInvalidException) {
                // 登录信息失效，跳转到登录页
                EventBus.getDefault().post(LoginFailedNotify(false))
            }
            return throwable
        }
        if (throwable is SocketTimeoutException) {
            return TimeoutException(
                mApplication.getString(R.string.http_server_out_time), throwable
            )
        }
        if (throwable is UnknownHostException) {
            val info =
                (mApplication.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            // 判断网络是否连接
            return if (info != null && info.isConnected) {
                // 有连接就是服务器的问题
                ServerException(mApplication.getString(R.string.http_server_error), throwable)
            } else NetworkException(mApplication.getString(R.string.http_network_error), throwable)
            // 没有连接就是网络异常
        }
        return if (throwable is IOException) {
            // 出现该异常的两种情况
            // 1. 调用 EasyHttp 取消请求
            // 2. 网络请求被中断
            CancelException(mApplication.getString(R.string.http_request_cancel), throwable)
        } else HttpException(throwable.message, throwable)
    }

    override fun downloadFail(httpRequest: HttpRequest<*>, throwable: Throwable): Throwable {
        when (throwable) {
            is ResponseException -> {
                val response = throwable.response
                throwable.setMessage(
                    mApplication.getString(R.string.http_response_error) + "，responseCode：" + response.code + "，message：" + response.message
                )
                return throwable
            }

            is NullBodyException -> {
                throwable.setMessage(mApplication.getString(R.string.http_response_null_body))
                return throwable
            }

            is FileMd5Exception -> {
                throwable.setMessage(mApplication.getString(R.string.http_response_md5_error))
                return throwable
            }

            else -> return requestFail(httpRequest, throwable)
        }
    }

    override fun readCache(httpRequest: HttpRequest<*>, type: Type, cacheTime: Long): Any? {
        val cacheKey = generateCacheKey(httpRequest)
        val cacheValue = readHttpCache(cacheKey)
        if (cacheValue == null || "" == cacheValue || "{}" == cacheValue) {
            return null
        }
        EasyLog.printLog(httpRequest, "----- read cache key -----")
        EasyLog.printJson(httpRequest, cacheKey)
        EasyLog.printLog(httpRequest, "----- read cache value -----")
        EasyLog.printJson(httpRequest, cacheValue)
        EasyLog.printLog(httpRequest, "cacheTime = $cacheTime")
        val cacheInvalidate = isCacheInvalidate(cacheKey, cacheTime)
        EasyLog.printLog(httpRequest, "cacheInvalidate = $cacheInvalidate")
        return if (cacheInvalidate) {
            // 表示缓存已经过期了，直接返回 null 给外层，表示缓存不可用
            null
        } else GsonFactory.getSingletonGson().fromJson<Any>(cacheValue, type)
    }

    override fun writeCache(httpRequest: HttpRequest<*>, response: Response, result: Any): Boolean {
        val cacheKey = generateCacheKey(httpRequest)
        val cacheValue = GsonFactory.getSingletonGson().toJson(result)
        if (cacheValue == null || "" == cacheValue || "{}" == cacheValue) {
            return false
        }
        EasyLog.printLog(httpRequest, "----- write cache key -----")
        EasyLog.printJson(httpRequest, cacheKey)
        EasyLog.printLog(httpRequest, "----- write cache value -----")
        EasyLog.printJson(httpRequest, cacheValue)
        val writeHttpCacheResult = writeHttpCache(cacheKey, cacheValue)
        EasyLog.printLog(httpRequest, "writeHttpCacheResult = $writeHttpCacheResult")
        val refreshHttpCacheTimeResult = setHttpCacheTime(cacheKey, System.currentTimeMillis())
        EasyLog.printLog(httpRequest, "refreshHttpCacheTimeResult = $refreshHttpCacheTimeResult")
        return writeHttpCacheResult && refreshHttpCacheTimeResult
    }

    override fun clearCache() {
        HttpCacheManager.clearCache()
    }
}