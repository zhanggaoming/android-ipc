package com.demo.ipc

import android.util.Log
import com.ipc.extend.test.InfoService
import com.ipc.extend.test.UserInfo
import com.zclever.ipc.core.Result
import com.zclever.ipc.core.TAG
import kotlin.concurrent.thread


object InfoServiceManager : InfoService {

    //获取userInfo，走的是回调的方式
    override fun asyncGetUserInfo(callBack: Result<UserInfo>) {
        thread {
            callBack.onSuccess(UserInfo("asyncGetUserInfo", 20))
        }
    }


    override fun syncGetUserInfo(): UserInfo {
        return UserInfo("syncGetUserInfo", 18)
    }


    override fun sum(a: Int, b: Int, c: Int, result: Result<Int>) {
        result.onSuccess(a + b + c)
    }

    override fun sendBigData(data: ByteArray) {
        Log.i(TAG, "sendBigData: ${data.contentToString()}")
    }
}