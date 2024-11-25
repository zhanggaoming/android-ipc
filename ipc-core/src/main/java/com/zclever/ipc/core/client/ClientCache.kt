package com.zclever.ipc.core.client

import android.os.ParcelFileDescriptor
import com.zclever.ipc.core.DataCallBack
import com.zclever.ipc.core.shared_memory.AbstractSharedMemory
import kotlin.reflect.KClass

/**
 * 客户端缓存类
 */
internal object ClientCache {

    val dataCallback = HashMap<String, DataCallBack>() //保存函数调用进来的回调实例，函数的signature=>DataCallBack

    val instanceMap = HashMap<KClass<*>, Any>()//存储动态代理创建的接口实例

    var clientSharedMemory: AbstractSharedMemory? = null //客户端创建的共享内存

    var serverCallbackSharedMemory: ParcelFileDescriptor? = null //服务端创建的回调使用的共享内存映射的实例

    var serverResponseSharedMemory: ParcelFileDescriptor? = null//服务端创建的直接回复使用的共享内存映射的实例

    var bigDataClientSharedMemory: AbstractSharedMemory? = null//客户端创建的用于传输bigdata的共享内存映射实例


}
