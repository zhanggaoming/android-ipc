package com.zclever.ipc.core.client

import com.zclever.ipc.core.DataCallBack
import com.zclever.ipc.core.memoryfile.IpcSharedMemory
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

/**
 * 客户端缓存类
 */
internal object ClientCache {

    val dataCallBack = HashMap<String, DataCallBack>() //保存方法调用进来的回调实例，一旦回调被调用过后，就清除相应实例

    val sharedMemoryMap = HashMap<SharedMemoryType, IpcSharedMemory>()//key取值0自身，1服务端

    val instanceMap = HashMap<KClass<*>, Any>()//存储接口和实例

}

enum class SharedMemoryType {
    CLIENT, SERVER
}