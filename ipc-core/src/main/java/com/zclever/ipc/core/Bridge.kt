package com.zclever.ipc.core

import android.os.Process
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


const val REQUEST_TYPE_CREATE = 1
const val REQUEST_TYPE_INVOKE = 2

/**
 * 请求对象
 */
data class Request(
    val type: Int = REQUEST_TYPE_INVOKE, //调用类型，分为构造服务实例和方法调用
    val targetClazzName: String, //目标服务实现类类名
    val functionKey: String = "", //方法id，就是KFunction的signature()
    val valueParametersMap: Map<String, String> = HashMap(), //参数具体值，name=>值的json字符串
    val invokeID: String = "", //回调方式的唯一标识
    val dataType: String = "", //回调的数据类的类型，其实就是我们写的泛型信息，就是我们写的泛型的实参的类的全名称
    val pid: Int = Process.myPid(),
    val sharedMemoryParameterIndex: Int = -1,//共享内存下标
    val sharedMemoryLength: Int = 0//共享内存存的数据实际长度
)


/**
 * 响应对象
 */
data class Response(var invokeID: String, val data: Any) {

    constructor(data: Any) : this("", data)

}