package com.tt.dramatime.aop

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
/**
 * <pre>
 *   @author : wiggins
 *   Date:  2024/2/6
 *   desc : 权限申请注解
 * </pre>
 */
@AndroidAopPointCut(PermissionsAspect::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER)
annotation class Permissions(
    /**
     * 需要申请权限的集合
     */
    vararg val value: String
)