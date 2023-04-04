package com.zclever.ipc.core.client

import com.zclever.ipc.IClient
import com.zclever.ipc.core.CallbackResponse
import com.zclever.ipc.core.GsonInstance
import com.zclever.ipc.core.debugD
import com.zclever.ipc.core.shared_memory.readJsonStr

/**
 * 对于客户端来讲的，这里是接受服务端返回的回调数据
 */
internal object Client : IClient.Stub() {

    override fun onReceive(responseJson: String) {

        var callbackResponse = GsonInstance.fromJson<CallbackResponse>(responseJson)

        if (callbackResponse.data.isNullOrEmpty()) {
            callbackResponse = CallbackResponse(
                callbackResponse.invokeKey,
                ClientCache.serverCallbackSharedMemory!!.readJsonStr(callbackResponse.dataByteSize)
            )
        }
        //接收到服务端给的回调数据后，转成Response给缓存的回调，从而返回了客户端
        debugD("onReceive: -->$responseJson")
        ClientCache.dataCallback[callbackResponse.invokeKey]?.onResponse(callbackResponse)
    }


}