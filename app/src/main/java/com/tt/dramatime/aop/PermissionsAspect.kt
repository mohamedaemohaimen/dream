package com.tt.dramatime.aop

import android.app.Activity
import androidx.fragment.app.Fragment
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.hjq.permissions.XXPermissions
import com.tt.dramatime.manager.ActivityManager
import com.tt.dramatime.other.PermissionCallback
import timber.log.Timber

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/6
 *   desc : 权限申请切面
 * </pre>
 */
class PermissionsAspect : BasePointCut<Permissions> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: Permissions): Any? {
        aroundJoinPoint(joinPoint, anno)
        // 关于 ProceedJoinPoint 可以看wiki 文档，详细点击链接
        // https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint
        return null
    }

    /**
     * 在连接点进行方法替换
     */
    private fun aroundJoinPoint(joinPoint: ProceedJoinPoint, permission: Permissions) {
        var activity: Activity? = null

        // 方法参数值集合
        val target = joinPoint.target
        val permissions: Array<out String> = permission.value

        if (target is Activity) {
            activity = target
        } else if (target is Fragment) {
            activity = target.activity
        }

        if ((activity == null) || activity.isFinishing || activity.isDestroyed) {
            activity = ActivityManager.getInstance().getTopActivity()
        }
        if ((activity == null) || activity.isFinishing || activity.isDestroyed) {
            Timber.e("The activity has been destroyed and permission requests cannot be made")
            return
        }

        requestPermissions(joinPoint, activity, permissions)
    }

    private fun requestPermissions(
        joinPoint: ProceedJoinPoint, activity: Activity, permissions: Array<out String>
    ) {
        XXPermissions.with(activity).permission(*permissions)
            .request(object : PermissionCallback() {

                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        try {
                            // 获得权限，执行原方法
                            joinPoint.proceed()
                        } catch (e: Throwable) {
                            Firebase.crashlytics.recordException(e)
                        }
                    }
                }
            })
    }

}