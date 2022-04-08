package com.zclever.ipc.core

import java.lang.reflect.ParameterizedType

interface DataCallBack {
    fun onResponse(data: Response)
}


/**
 * 统一抽象回调数据类型
 */
abstract class Result<T> : DataCallBack {

    //获取泛型的实际类型,每个类只有一份,用于反序列化
    private val dataClass by lazy {
        obtainDataClass(this::class.java)
        //this::class.supertypes.first { it.classifier == Result::class }.arguments[0].type!!.classifier.safeAs<KClass<*>>()!!.java
    }

    private val dataType by lazy {
        GsonInstance.obtainSuperclassTypeParameter(javaClass)!!
    }

    companion object {
        fun obtainDataClass(clazz: Class<*>): Class<*> {
            val superClass = clazz.genericSuperclass

            //println("---------->$superClass")

            var superClazz = when (superClass) {
                is ParameterizedType -> {
                    superClass.rawType as Class<*>
                }
                is Class<*> -> {
                    superClass
                }
                else ->
                    throw IllegalArgumentException("the type $superClass is not be supported！")

            }
            //println("<----------$superClazz")

            if (superClazz == Result::class.java) {

                val returnClass =
                    (superClass as ParameterizedType).actualTypeArguments.first().let {
                        (it as? Class<*>) ?: ((it as? ParameterizedType)?.rawType as Class<*>)
                    }
                debugD(returnClass.toString())
                return returnClass
            }

            if (superClazz == Any::class.java || superClazz == Object::class.java) {
                throw IllegalArgumentException("can not find the dataclass!")
            }

            return obtainDataClass(superClazz)
        }
    }


    override fun onResponse(data: Response) {

        onData(
            GsonInstance.fromJson<T>(GsonInstance.toJson(data.data), dataType)
        )
    }

    abstract fun onData(data: T)
}
