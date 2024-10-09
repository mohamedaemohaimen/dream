package com.tt.dramatime.aop

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/06
 *    desc   : Debug 日志注解
 */
@AndroidAopPointCut(LogAspect::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER)
annotation class Log(val value: String = "AppLog")