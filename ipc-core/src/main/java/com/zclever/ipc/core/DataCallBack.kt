package com.zclever.ipc.core

import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.*
import kotlin.getValue
import kotlin.lazy

internal interface DataCallBack {
    fun onResponse(data: CallbackResponse)
}


/**
 * 统一抽象回调数据类型
 */
abstract class Result<T> : DataCallBack {

    //获取泛型的实际类型,用于反序列化
    private val dataType by lazy {
        obtainDataType()
        //this::class.allSupertypes.first { it.classifier == Result::class }.arguments[0].type!!
    }

    override fun onResponse(data: CallbackResponse) {

        onData(
            GsonInstance.fromJson<T>(data.data, dataType)
        )
    }

    abstract fun onData(data: T)

    private fun obtainDataType(): Type {

        val superType = this::class.java.genericSuperclass

        if (superType!!.rawType() != Result::class.java) {
            throw IllegalStateException("the class ${this::class} must only inherit  ${Result::class.qualifiedName}!!")
        }

        val parameterized = superType as ParameterizedType
        return `$Gson$Types`.canonicalize(parameterized.actualTypeArguments[0])
    }

}


fun Type.rawType(): Class<*> = `$Gson$Types`.getRawType(this)