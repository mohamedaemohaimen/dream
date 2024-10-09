package com.tt.dramatime.manager

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.collection.ArrayMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.orhanobut.logger.Logger
import com.tt.base.BaseActivity
import com.tt.dramatime.app.AppApplication
import com.tt.dramatime.http.db.CommonAdConfigHelper
import com.tt.dramatime.manager.google.ad.AppOpenAdManager
import timber.log.Timber

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/1/4
 *   desc : Activity 管理类
 * </pre>
 */
class ActivityManager private constructor() : ActivityLifecycleCallbacks {

    companion object {

        @Suppress("StaticFieldLeak")
        private val activityManager: ActivityManager by lazy { ActivityManager() }

        fun getInstance(): ActivityManager {
            return activityManager
        }

        /**
         * 获取一个对象的独立无二的标记
         */
        private fun getObjectTag(`object`: Any): String {
            // 对象所在的包名 + 对象的内存地址
            return `object`.javaClass.name + Integer.toHexString(`object`.hashCode())
        }
    }

    /** Activity 存放集合 */
    private val activitySet: ArrayMap<String?, Activity?> = ArrayMap()

    /** 应用生命周期回调 */
    private val lifecycleCallbacks: ArrayList<ApplicationLifecycleCallback> = ArrayList()

    /** 当前应用上下文对象 */
    private lateinit var application: Application

    /** 栈顶的 Activity 对象 */
    private var topActivity: Activity? = null

    /** 前台并且可见的 Activity 对象 */
    private var resumedActivity: Activity? = null

    private var currentActivity: Activity? = null
    private var lastShowOpenAdTime: Long = 0

    private var interval = 4 * 60 * 60 * 1000

    fun init(application: Application) {
        this.application = application
        this.application.registerActivityLifecycleCallbacks(this)

        // 注册LifecycleEventObserver来监听应用进入前台的事件
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_START) {
                    Logger.e("ProcessLifecycleOwner.ON_START:应用进入前台")
                    interval = CommonAdConfigHelper.getOpenAdIntervalFreq() * 1000
                    currentActivity?.let {
                        for (callback: ApplicationLifecycleCallback in lifecycleCallbacks) {
                            callback.onApplicationForeground(it)
                        }
                        // 判断上次显示广告与本次现实广告的时间间隔，避免两次广告之间间隔太短
                        //屏蔽IAA模块代码
                        if (CommonAdConfigHelper.getOpenAdEnableStatus() && (lastShowOpenAdTime == 0L || System.currentTimeMillis() - lastShowOpenAdTime > interval)) {
                            Logger.d("ProcessLifecycleOwner.ON_START:开屏广告显示")
                            (application as AppApplication).appOpenAdManager?.showAppOpenAd(it,
                                object : AppOpenAdManager.AppOpenAdShowCallback {
                                    override fun onAppOpenAdShow() {
                                        lastShowOpenAdTime = System.currentTimeMillis()
                                    }

                                    override fun onAppOpenAdShowComplete() {}
                                })
                        }
                    }
                } else if (event == Lifecycle.Event.ON_STOP) {
                    Logger.d("ProcessLifecycleOwner.ON_STOP:应用进入后台")
                    for (callback: ApplicationLifecycleCallback in lifecycleCallbacks) {
                        currentActivity?.let { callback.onApplicationBackground(it) }
                    }
                }
            }
        })
    }

    /**
     * 获取 Application 对象
     */
    fun getApplication(): Application {
        return application
    }

    /**
     * 获取栈顶的 Activity
     */
    fun getTopActivity(): Activity? {
        return topActivity
    }

    /**
     * 获取指定的 Activity
     */
    fun getActivity(clazz: Class<out Activity?>): Activity? {

        val keys: Array<String?> = activitySet.keys.toTypedArray()
        for (key: String? in keys) {
            val activity: Activity? = activitySet[key]
            if (activity == null || activity.isFinishing) {
                continue
            }
            if ((activity.javaClass == clazz)) {
                return activity
            }
        }
        return null
    }

    /**
     * 获取所有的 Activity
     */
    fun getAllActivity(): ArrayMap<String?, Activity?> {
        return activitySet
    }


    /**
     * 获取前台并且可见的 Activity
     */
    fun getResumedActivity(): Activity? {
        return resumedActivity
    }

    /**
     * 判断当前应用是否处于前台状态
     */
    fun isForeground(): Boolean {
        return getResumedActivity() != null
    }

    /**
     * 注册应用生命周期回调
     */
    fun registerApplicationLifecycleCallback(callback: ApplicationLifecycleCallback) {
        lifecycleCallbacks.add(callback)
    }

    /**
     * 取消注册应用生命周期回调
     */
    fun unregisterApplicationLifecycleCallback(callback: ApplicationLifecycleCallback) {
        lifecycleCallbacks.remove(callback)
    }

    /**
     * 销毁指定的 Activity
     */
    fun finishActivity(clazz: Class<out Activity?>?) {
        if (clazz == null) {
            return
        }
        val keys: Array<String?> = activitySet.keys.toTypedArray()
        for (key: String? in keys) {
            val activity: Activity? = activitySet[key]
            if (activity == null || activity.isFinishing) {
                continue
            }
            if ((activity.javaClass == clazz)) {
                activity.finish()
                activitySet.remove(key)
                break
            }
        }
    }

    /**
     * 销毁所有的 Activity
     */
    fun finishAllActivities() {
        finishAllActivities(null as Class<out Activity?>?)
    }

    /**
     * 销毁所有的 Activity
     *
     * @param classArray            白名单 Activity
     */
    @SafeVarargs
    fun finishAllActivities(vararg classArray: Class<out Activity>?) {
        val keys: Array<String?> = activitySet.keys.toTypedArray()
        for (key: String? in keys) {
            val activity: Activity? = activitySet[key]
            if (activity == null || activity.isFinishing) {
                continue
            }
            var whiteClazz = false
            for (clazz: Class<out Activity?>? in classArray) {
                if ((activity.javaClass == clazz)) {
                    whiteClazz = true
                }
            }
            if (whiteClazz) {
                continue
            }

            // 如果不是白名单上面的 Activity 就销毁掉
            activity.finish()
            activitySet.remove(key)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Timber.i("%s - onCreate", activity.javaClass.simpleName)
        if (activitySet.size == 0) {
            for (callback: ApplicationLifecycleCallback? in lifecycleCallbacks) {
                callback?.onApplicationCreate(activity)
            }
            Timber.i("%s - onApplicationCreate", activity.javaClass.simpleName)
        }
        activitySet[getObjectTag(activity)] = activity
        topActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        Timber.i("%s - onStart", activity.javaClass.simpleName)
        // 开屏广告不展示的情况下，记录当前的Activity
        if ((application as AppApplication).appOpenAdManager?.showingAd != true) {
            if (activity is BaseActivity) {
                currentActivity = activity
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        Timber.i("%s - onResume", activity.javaClass.simpleName)
        /*if (topActivity === activity && resumedActivity == null) {
            for (callback: ApplicationLifecycleCallback in lifecycleCallbacks) {
                callback.onApplicationForeground(activity)
            }
            Timber.i("%s - onApplicationForeground", activity.javaClass.simpleName)
        }*/
        topActivity = activity
        resumedActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        Timber.i("%s - onPause", activity.javaClass.simpleName)
    }

    override fun onActivityStopped(activity: Activity) {
        Timber.i("%s - onStop", activity.javaClass.simpleName)
        if (resumedActivity === activity) {
            resumedActivity = null
        }
        if (resumedActivity == null) {
            /* for (callback: ApplicationLifecycleCallback in lifecycleCallbacks) {
                 callback.onApplicationBackground(activity)
             }
             Timber.i("%s - onApplicationBackground", activity.javaClass.simpleName)*/
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Timber.i("%s - onSaveInstanceState", activity.javaClass.simpleName)
    }

    override fun onActivityDestroyed(activity: Activity) {
        Timber.i("%s - onDestroy", activity.javaClass.simpleName)
        activitySet.remove(getObjectTag(activity))
        if (topActivity === activity) {
            topActivity = null
        }
        if (activitySet.size == 0) {
            for (callback: ApplicationLifecycleCallback in lifecycleCallbacks) {
                callback.onApplicationDestroy(activity)
            }
            Timber.i("%s - onApplicationDestroy", activity.javaClass.simpleName)
        }
    }

    /**
     * 应用生命周期回调
     */
    interface ApplicationLifecycleCallback {

        /**
         * 第一个 Activity 创建了
         */
        fun onApplicationCreate(activity: Activity)

        /**
         * 最后一个 Activity 销毁了
         */
        fun onApplicationDestroy(activity: Activity)

        /**
         * 应用从前台进入到后台
         */
        fun onApplicationBackground(activity: Activity)

        /**
         * 应用从后台进入到前台
         */
        fun onApplicationForeground(activity: Activity)
    }
}