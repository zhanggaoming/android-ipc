package com.zclever.ipc.annotation

/**
 * 用于标记大数据的注解，目前设计只能用于修饰方法形参，并且只能是字节数组,其他类型不支持
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER,AnnotationTarget.FIELD)
annotation class BigData
