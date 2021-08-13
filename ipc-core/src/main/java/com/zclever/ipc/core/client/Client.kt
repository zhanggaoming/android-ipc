package com.zclever.ipc.core.client

import android.util.Log
import com.zclever.ipc.IClient
import com.zclever.ipc.core.GsonInstance
import com.zclever.ipc.core.Response
import com.zclever.ipc.core.TAG

/**
 * 对于客户端来讲的，这里是接受服务端返回的回调数据
 */
internal object Client : IClient.Stub() {

    override fun onReceive(data: String?) {
        Log.i(TAG, "onReceive: $data")
        //接收到服务端给的回调数据后，转成Response给缓存的回调，从而返回了客户端
        GsonInstance.gson.fromJson(data, Response::class.java)?.let { response ->

            Log.i(TAG, "onReceive: -->${ClientCache.dataCallBack[response.invokeID]}")
            ClientCache.dataCallBack[response.invokeID]?.onResponse(response)

        }
    }

}