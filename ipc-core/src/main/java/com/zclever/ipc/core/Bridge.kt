package com.zclever.ipc.core

import android.os.Process


const val REQUEST_TYPE_CREATE = 1
const val REQUEST_TYPE_INVOKE = 2
const val BINDER_MAX_TRANSFORM_PARCEL_SIZE = 600_000 //定义最大Parcel传输的size，理论是1M-8k，这里需要写小一点

/**
 * 请求对象
 */
data class RequestBase(
    val type: Int = REQUEST_TYPE_INVOKE, //调用类型，分为构造服务实例和方法调用
    val targetClazzName: String, //目标服务实现类类名
    val pid: Int = Process.myPid(),
    val functionKey: String = "", //方法id，就是KFunction的signature()
    val callbackKey: String = "", //异步回调实体的Map的key
    val transformType: TransformType = TransformType.BINDER,//传输方式
    val paramValueBytesLen: Int = 0, // 参值json序列化后的utf-8编码后的字节长度
    val useBigIndex:Boolean=false,
    val bigIndexParamName:String=""
)




data class RequestParam(
    val paramValueMap: Map<String, String> = HashMap(), //参数具体值，name=>值的json字符串
)

internal fun RequestBase.createNoParameterRequest(paramValueBytesLen: Int) =
    RequestBase(
        type,
        targetClazzName,
        pid,
        functionKey,
        callbackKey,
        TransformType.SHARED_MEMORY,
        paramValueBytesLen,
    )

enum class TransformType {
    BINDER, SHARED_MEMORY
}

/**
 * 直接返回响应对象
 */
data class Response(val dataStr: String?, val dataByteSize: Int = -1)

/**
 * 回调响应对象
 */
data class CallbackResponse(var invokeKey: String, val data: String?, val dataByteSize: Int = 0)
