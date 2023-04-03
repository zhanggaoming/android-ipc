package com.zclever.ipc.core

import kotlin.reflect.full.allSupertypes

internal interface DataCallBack {
   fun onResponse(data: CallbackResponse)
}


/**
 * 统一抽象回调数据类型
 */
abstract class Result<T> : DataCallBack {

    //获取泛型的实际类型,用于反序列化
    private val dataType by lazy {
//        GsonInstance.obtainSuperclassTypeParameter(javaClass)!!
        this::class.allSupertypes.first { it.classifier == Result::class }.arguments[0].type!!
    }

    override fun onResponse(data: CallbackResponse) {

        onData(
            GsonInstance.fromJson<T>(data.data, dataType)
        )
    }

    abstract fun onData(data: T)
}
