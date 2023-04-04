package com.zclever.ipc.core.server

import android.os.ParcelFileDescriptor
import com.zclever.ipc.IClient
import com.zclever.ipc.core.shared_memory.AbstractSharedMemory
import kotlin.reflect.KFunction

/**
 * 服务端缓存
 */
internal object ServiceCache {

    val kFunctionMap = HashMap<String, Map<String, KFunction<*>>>() //存实现类的KFunction

    val kInstanceMap = HashMap<String, Any?>() //存实现类对应的实例,只允许单例

    val serverCallbackMemoryMap: MutableMap<Int, AbstractSharedMemory> =
        HashMap() //服务端缓存回调共享内存 pid=>MemoryFile

    val serverResponseMemoryMap: MutableMap<Int, AbstractSharedMemory> =
        HashMap() //服务端直接返回的共享内存map pid=>MemoryFile

    val clientSharedMemoryMap: MutableMap<Int, ParcelFileDescriptor> =
        HashMap() //客户端创建的共享内存映射的实例pid=>ParcelFileDescriptor

    val remoteClients: RemoteClientList<IClient> = RemoteClientList(
        serverCallbackMemoryMap,
        serverResponseMemoryMap,
        clientSharedMemoryMap
    ) //维护客户端列表

    var videoService: VideoService? = null


}

