package com.tt.dramatime.other

import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.config.IToastInterceptor
import com.tt.dramatime.action.ToastAction
import timber.log.Timber


/**
 *  author : Android 轮子哥
 *  github : https://github.com/getActivity/AndroidProject-Kotlin
 *  time   : 2020/11/04
 *  desc   : 自定义 Toast 拦截器（用于追踪 Toast 调用的位置）
 */
class ToastLogInterceptor : IToastInterceptor {

    override fun intercept(text: ToastParams): Boolean {
        if (AppConfig.isLogEnable()) {
            // 获取调用的堆栈信息
            val stackTrace: Array<StackTraceElement> = Throwable().stackTrace
            // 跳过最前面两个堆栈
            var i = 2
            while (stackTrace.size > 2 && i < stackTrace.size) {

                // 获取代码行数
                val lineNumber: Int = stackTrace[i].lineNumber
                // 获取类的全路径
                val className: String = stackTrace[i].className
                if (((lineNumber <= 0) || className.startsWith(Toaster::class.java.name) ||
                            className.startsWith(ToastAction::class.java.name))) {
                    i++
                    continue
                }
                Timber.tag("Toaster")
                Timber.i("(%s:%s) %s", stackTrace[i].fileName, lineNumber, text.toString())
                break
                i++
            }
        }
        return false
    }
}