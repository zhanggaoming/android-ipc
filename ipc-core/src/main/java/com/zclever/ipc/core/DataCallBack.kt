package com.zclever.ipc.core

import android.os.SystemClock
import android.util.Log
import com.zclever.ipc.core.client.ClientCache
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

        if (data.success) {
            onSuccess(
                GsonInstance.gson.fromJson(GsonInstance.gson.toJson(data.data), dataClass)
                    .safeAs<T>()!!
            )
        } else {
            onFailure(GsonInstance.gson.toJson(data.data))
        }
        //返回结果之后移除实例，防止内存泄露
        ClientCache.dataCallBack.entries.filter { it.value == this }
            .forEach { ClientCache.dataCallBack.remove(it.key) }
    }


    abstract fun onSuccess(data: T)

    abstract fun onFailure(message: String)
}
