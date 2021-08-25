package com.zclever.ipc.core

import com.google.gson.Gson
import kotlin.reflect.KClass

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

    fun fromJson(json: String?, dataClass: KClass<*>) = fromJson(json, dataClass.java)

}


