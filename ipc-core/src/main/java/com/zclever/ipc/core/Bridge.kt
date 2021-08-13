package com.zclever.ipc.core

import android.os.MemoryFile
import android.os.Process
import java.util.concurrent.atomic.AtomicInteger


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
    val invokeID: Int = -1, //回调方式的唯一id
    val dataType: String = "", //回调的数据类的类型，其实就是我们写的泛型信息，就是我们写的泛型的实参的类的全名称
    val pid: Int = Process.myPid()
) {


    internal companion object {
        val invokeId: AtomicInteger = AtomicInteger(0) //计算方法的唯一id，不断累加
    }
}


/**
 * 响应对象
 */
data class Response(val success: Boolean, var invokeID: Int, val data: Any) {

    constructor(success: Boolean, data: Any) : this(success, 0, data)

}

data class MemoryFileResponse(
    val memoryFile: MemoryFile, val width: Int, val height: Int, val availableSize: Int
)