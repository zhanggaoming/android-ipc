package com.zclever.ipc.core.server

import com.zclever.ipc.core.*
import com.zclever.ipc.core.GsonInstance
import com.zclever.ipc.core.shared_memory.writeByteArray
import java.lang.UnsupportedOperationException

/**
 * 服务端伪造回调类
 */
internal class ServerCallBack(
    private val pid: Int, private val callbackKey: String
) : Result<Any>() {

    override fun onResponse(data: CallbackResponse) {
        throw UnsupportedOperationException("the server can't operate onResponse function!")
    }


    //服务端实现调用onData给客户端传数据，实现异步回调
    override fun onData(data: Any) {

        val dataJson = GsonInstance.toJson(data)

        val dataJsonByteArray=dataJson.encodeToByteArray()

        debugD("onData: $callbackKey,-------${ServiceCache.remoteClients.getClientByPid(pid)}，size->${dataJsonByteArray.size}")

        if (dataJsonByteArray.size < BINDER_MAX_TRANSFORM_JSON_BYTE_ARRAY_SIZE) {
            debugD("onData use binder")
            ServiceCache.remoteClients.getClientByPid(pid)
                ?.onReceive(GsonInstance.toJson(CallbackResponse(callbackKey, dataJson)))

        } else {
            debugD("onData use shared memory")
            val sharedMemory = ServiceCache.serverCallbackMemoryMap[pid]!!

            synchronized(sharedMemory) {
                ServiceCache.remoteClients.getClientByPid(pid)?.onReceive(
                        GsonInstance.toJson(
                            CallbackResponse(
                                callbackKey,
                                null,
                                dataJsonByteArray
                                    .also { sharedMemory.writeByteArray(it) }.size
                            )
                        )
                    )
            }
        }
    }
}