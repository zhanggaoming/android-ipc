package com.zclever.ipc.core

import com.google.gson.Gson
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

/**
 * 采用gson序列化，moshi之前测试还有问题
 */
internal object GsonInstance {

//    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

//    fun toJson(obj: Any?) = moshi.adapter(obj!!.javaClass).toJson(obj)
//
//    fun fromJson(json: String?, dataClass: Class<*>) = moshi.adapter(dataClass).fromJson(json)
//
//    fun fromJson(json: String?, dataClass: KClass<*>) = fromJson(json, dataClass.java)
//
//    private fun Moshi.adapter(clazz: Class<*>) = adapter<Any>(clazz)

    private val gson = Gson()

    fun toJson(obj: Any?) = gson.toJson(obj)

    fun fromJson(json: String?, dataClass: Class<*>) = gson.fromJson(json, dataClass)

    fun <T> fromJson(json: String?, type: Type) = gson.fromJson<T>(json, type)

    fun <T> fromJson(json: String?, type: KType) = gson.fromJson<T>(json, type.javaType)

    inline fun <reified T> fromJson(json: String?) = gson.fromJson(json,T::class.java)

    fun obtainSuperclassTypeParameter(subclass: Class<*>): Type? {
        val superclass: Type = subclass.genericSuperclass
        if (superclass is Class<*>) {
            throw RuntimeException("Missing type parameter.")
        }
        val parameterized = superclass as ParameterizedType
        return `$Gson$Types`.canonicalize(parameterized.actualTypeArguments[0])
    }

}




