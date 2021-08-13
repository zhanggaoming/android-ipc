package com.zclever.ipc.core.server

import android.util.Log
import com.zclever.ipc.core.GsonInstance
import com.zclever.ipc.core.Response
import com.zclever.ipc.core.Result
import com.zclever.ipc.core.TAG
import java.lang.UnsupportedOperationException

/**
 * 服务端伪造回调类
 */
internal class ServiceCallBack(
    private val pid: Int, private val invokeID: Int, val dataType: String
) : Result<Any>() {

    override fun onResponse(data: Response) {
        throw UnsupportedOperationException("the server can't operate onResponse function!")
    }


    override fun onSuccess(data: Any) {

        Log.i(TAG, "onSuccess: $invokeID,-------${ServiceCache.remoteClients.getClientByPid(pid)}")

        val response = Response(true, invokeID, data)

        ServiceCache.remoteClients.getClientByPid(pid)
            ?.onReceive(GsonInstance.gson.toJson(response))

    }

    override fun onFailure(message: String) {

        Log.i(TAG, "onFailure: $invokeID,-------${ServiceCache.remoteClients.getClientByPid(pid)}")

        val response = Response(false, invokeID, message)

        ServiceCache.remoteClients.getClientByPid(pid)
            ?.onReceive(GsonInstance.gson.toJson(response))
    }


}