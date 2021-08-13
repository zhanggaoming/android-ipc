package com.zclever.ipc.core.server

import android.os.MemoryFile
import com.zclever.ipc.IClient
import kotlin.reflect.KFunction

/**
 * 服务端缓存
 */
internal object ServiceCache {

    val kFunctionMap = HashMap<String, Map<String, KFunction<*>>>() //存实现类的KFunction

    val kInstanceMap = HashMap<String, Any?>() //存实现类对应的实例,只允许单例

    val remoteClients: RemoteClientList<IClient> = RemoteClientList() //维护客户端列表

    val memoryFileMap = HashMap<Long, MemoryFile>()

    var videoService: VideoService? = null
}

