package com.zclever.ipc.core

import com.google.gson.Gson

/**
 * 采用Gson来序列化，本来想用Moshi来做的，但是Moshi需要把泛型的实例化写上
 * 用Gson的话要注意kotlin的一些语法特性，gson是不支持的
 */
internal object GsonInstance {

    val gson = Gson()

}