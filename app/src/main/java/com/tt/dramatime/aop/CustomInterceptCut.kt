package com.tt.dramatime.aop

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut

class CustomInterceptCut : BasePointCut<CustomIntercept> {
    //annotation就是你加到方法上的注解
    override fun invoke(joinPoint: ProceedJoinPoint, anno: CustomIntercept): Any? {
        // 在此写你的逻辑
        // joinPoint.proceed() 表示继续执行切点方法的逻辑，不调用此方法不会执行切点方法里边的代码
        // 关于 ProceedJoinPoint 可以看wiki 文档，详细点击下方链接
        // https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint
        return joinPoint.proceed()
    }
}