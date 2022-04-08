package com.zclever.ipc.core.server

import com.zclever.ipc.core.*
import com.zclever.ipc.core.GsonInstance
import java.lang.UnsupportedOperationException

/**
 * 服务端伪造回调类
 */
internal class ServiceCallBack(
    private val pid: Int, private val invokeID: String, val dataType: String
) : Result<Any>() {

    override fun onResponse(data: Response) {
        throw UnsupportedOperationException("the server can't operate onResponse function!")
    }


    override fun onData(data: Any) {

        debugI("onData: $invokeID,-------${ServiceCache.remoteClients.getClientByPid(pid)}")

        val response = Response(invokeID, data)

        ServiceCache.remoteClients.getClientByPid(pid)
            ?.onReceive(GsonInstance.toJson(response))

    }



}