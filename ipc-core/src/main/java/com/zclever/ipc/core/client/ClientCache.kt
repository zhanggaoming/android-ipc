package com.zclever.ipc.core.client

import com.zclever.ipc.core.DataCallBack

/**
 * 客户端缓存类
 */
internal object ClientCache {


    val dataCallBack = HashMap<Int, DataCallBack>() //保存方法调用进来的回调实例，一旦回调被调用过后，就清除相应实例


}