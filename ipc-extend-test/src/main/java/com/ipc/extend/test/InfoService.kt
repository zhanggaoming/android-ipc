package com.ipc.extend.test


import com.zclever.ipc.annotation.BindImpl
import com.zclever.ipc.core.Result

@BindImpl("com.demo.ipc.InfoServiceManager")
interface InfoService {

    fun asyncGetUserInfo(callBack: Result<UserInfo>)

    fun syncGetUserInfo(): UserInfo

    fun sum(a:Int,b:Int,c:Int,result: Result<Int>)

}


data class UserInfo(val name: String, val age: Int)