package com.ipc.extend.test


import com.zclever.ipc.annotation.BigData
import com.zclever.ipc.annotation.BindImpl
import com.zclever.ipc.core.Result

@BindImpl("com.demo.ipc.InfoServiceManager")
interface InfoService {

    fun asyncGetUserInfo(callBack: Result<UserInfo>)

    fun syncGetUserInfo(): UserInfo

    fun sum(a: Int, b: Int, c: Int, result: Result<Int>)

    fun sendBigData(@BigData data: ByteArray)

    fun getEnum(code: Code): Code

    fun setEventCallBack(callBack: Result<Event>)

    fun setResponeCallBack(callBack: Result<BaseRespone<Event>>)


    fun transformAreaBeans(wrapper: ArrayList<AreaBean>):Int

}


data class Data(val areaBeanList:ArrayList<AreaBean>)

enum class Code {
    SUCCESS, FAILURE
}

class AreaBean {

    var areaId: Int = 0


}


data class Event(val id: Int)


data class UserInfo(val name: String, val age: Int)

class BaseRespone<T>(val data: T) {

}