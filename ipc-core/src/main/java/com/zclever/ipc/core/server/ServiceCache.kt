package com.zclever.ipc.core.server

import android.os.MemoryFile
import com.zclever.ipc.IClient
import com.zclever.ipc.core.memoryfile.IpcSharedMemory
import kotlin.reflect.KFunction

/**
 * 服务端缓存
 */
internal object ServiceCache {

    val kFunctionMap = HashMap<String, Map<String, KFunction<*>>>() //存实现类的KFunction

    val kInstanceMap = HashMap<String, Any?>() //存实现类对应的实例,只允许单例

    val remoteClients: RemoteClientList<IClient> = RemoteClientList() //维护客户端列表

    val clientSharedMemoryMap = HashMap<Int, IpcSharedMemory>()//维护客户端提供给服务端使用的共享内存map

    val serverSharedMemoryMap = HashMap<Int, IpcSharedMemory>()//维护服务端提供给客户端使用的共享内存map

    var videoService: VideoService? = null


}

