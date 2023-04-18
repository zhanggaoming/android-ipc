package com.ipc.extend.test


import com.zclever.ipc.annotation.BindImpl
import com.zclever.ipc.core.Result

@BindImpl("com.demo.ipc.InfoServiceManager")
interface InfoService {

    fun asyncGetUserInfo(callBack: Result<UserInfo>)

    fun syncGetUserInfo(): UserInfo

    fun sum(a: Int, b: Int, c: Int, result: Result<Int>)

    fun sendBigData(data: ByteArray) //传大字节数据

    fun getEnum(code: Code): Code

    fun setEventCallBack(callBack: Result<Event>)

    fun setResponseCallBack(callBack: Result<BaseResponse<Event>>)  //设置泛型嵌套回调的方式

    fun transformAreaBeans(areaBeanList: ArrayList<AreaBean>): Int //传带泛型参数的类型数据

    fun getBigByteArray(): ByteArray //获取服务端返回的超大数据

    fun asyncGetBigByteArray(callBack: Result<ByteArray>) //异步获取超大数据
}

enum class Code {
    SUCCESS, FAILURE
}

class AreaBean {

    var areaId: Int = 0

}


data class Event(val id: Int)


data class UserInfo(val name: String, val age: Int)

class BaseResponse<T>(val data: T)

