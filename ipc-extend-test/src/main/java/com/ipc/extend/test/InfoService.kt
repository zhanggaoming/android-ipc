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

}

enum class Code {
    SUCCESS, FAILURE
}


data class Event(val id: Int)


data class UserInfo(val name: String, val age: Int)