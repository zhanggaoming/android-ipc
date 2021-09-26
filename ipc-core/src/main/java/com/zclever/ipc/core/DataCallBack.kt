package com.zclever.ipc.core

import kotlin.reflect.KClass

interface DataCallBack {
    fun onResponse(data: Response)
}


/**
 * 统一抽象回调数据类型
 */
abstract class Result<T> : DataCallBack {

    //获取泛型的实际类型,每个类只有一份,用于反序列化
    private val dataClass by lazy {
        this::class.supertypes.first { it.classifier == Result::class }.arguments[0].type!!.classifier.safeAs<KClass<*>>()!!.java
    }


    override fun onResponse(data: Response) {

        onData(
            GsonInstance.fromJson(GsonInstance.toJson(data.data), dataClass)
                .safeAs<T>()!!
        )

    }

    abstract fun onData(data: T)
}
